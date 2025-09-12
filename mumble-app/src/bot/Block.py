import torch
from torch import nn
from MultiHead import MultiHead
from FeedForward import FeedForward

class Block(nn.Module):

    def __init__(self, d_model, n_head, vocab_size, dropout, block_size, device, mask=True):
        super().__init__()

        head_size = d_model // n_head
        
        self.sa_heads = MultiHead(d_model, n_head, head_size, vocab_size, dropout, block_size, device, mask=True)
        self.ca_heads = MultiHead(d_model, n_head, head_size, vocab_size, dropout, block_size, device, mask=False)

        self.ln1 = nn.LayerNorm(d_model)
        self.ln2 = nn.LayerNorm(d_model)
        self.ln3 = nn.LayerNorm(d_model)

        self.ffwd = FeedForward(d_model, dropout, device)

    def forward(self, x, encoder_output=None):
        x = x + self.sa_heads(self.ln1(x))
        if encoder_output is not None:
            x = x + self.ca_heads(self.ln2(x), context=encoder_output)
        x = x + self.ffwd(self.ln3(x))
        return x

