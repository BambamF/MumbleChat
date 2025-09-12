import torch
from torch import nn
from Block import Block
from torch.nn import functional as F

class GPT_MODEL_6(nn.Module):

    def __init__(self, d_model, n_head, n_layer, vocab, reverse_vocab, dropout, block_size, device, max_len, mask=True):
        super().__init__()

        self.token_embedding = nn.Embedding(
            num_embeddings=len(vocab),
            embedding_dim=d_model,
            device=device
        )

        self.positional_embedding = nn.Embedding(
            num_embeddings=max_len,
            embedding_dim=d_model,
            device=device
        )

        self.proj = nn.Linear(
            in_features=d_model,
            out_features=len(vocab),
            device=device
        )

        self.vocab = vocab
        self.reverse_vocab = reverse_vocab
        self.n_layer = n_layer
        self.n_head = n_head
        self.max_len = max_len
        self.device = device

        self.encoder_blocks = nn.Sequential(
            *[Block(d_model, n_head, len(vocab), dropout, block_size, device, mask=True) for _ in range(self.n_layer)]
        )

        self.decoder_blocks = nn.ModuleList(
            [Block(d_model, n_head, len(vocab), dropout, block_size, device, mask=True) for _ in range(self.n_layer)]
        )

    def forward(self, encoder_input, decoder_input, targets=None):
        B, T_enc = encoder_input.shape
        B, T_dec = decoder_input.shape

        encoder_tokens = self.token_embedding(encoder_input)
        decoder_tokens = self.token_embedding(decoder_input)

        encoder_positional = self.positional_embedding(torch.arange(self.max_len, device=self.device))[:T_enc, :]
        decoder_positional = self.positional_embedding(torch.arange(T_dec, device=self.device))

        encoder_output = encoder_tokens + encoder_positional
        decoder_output = decoder_tokens + decoder_positional

        encoder_out = self.encoder_blocks(encoder_output)
        decoder_out = decoder_output
        for block in self.decoder_blocks:
            decoder_out = block(decoder_out, encoder_output=encoder_out)
        
        logits = self.proj(decoder_out)

        if targets is None:
            loss = None
            return logits, loss
        else:
            B, T, C = logits.shape

            logits = logits.view(B*T, C)
            targets = targets.view(B*T)

            loss = F.cross_entropy(logits, targets)
            return logits, loss

    def generate(self, encoder_input, max_new_tokens=15):

        start_symbol = self.vocab['<start>']
        end_symbol = self.vocab['<end>']
        pad_symbol = self.vocab['<pad>']
        unk_symbol = self.vocab['<unk>']

        padded_encoder_input = torch.full(
            size=(1, encoder_input.shape[0]), 
            fill_value=pad_symbol, 
            device=self.devuice, 
            dtype=torch.long)

        decoder_input = torch.tensor(
            data=[[start_symbol]],
            dtype=torch.long,
            device=self.device,
            requires_grad=False
        )
        
        padded_encoder_input[0: encoder_input.shape[0]] = encoder_input.squeeze(dim=0)

        for _ in range(max_new_tokens - 1):
            decoder_input_truncated = decoder_input[:, -max_new_tokens]
            logits, loss = self(padded_encoder_input, decoder_input_truncated)
            logits = logits[:, -1, :]
            probs = torch.softmax(logits, dim=-1)
            next_logits = torch.multinomial(probs, num_samples=1)
            decoder_input = torch.cat([decoder_input, next_logits], dim=1)
            if next_logits.item() == end_symbol:
                break
        tgt_tokens = decoder_input.squeeze().tolist()

        return " ".join([self.reverse_word_map.get(i, self.reverse_word_map[unk_symbol]) for i in tgt_tokens if i not in [start_symbol, end_symbol, pad_symbol]])

