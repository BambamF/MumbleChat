import torch
from torch import nn
from Head import Head

class MultiHead(nn.Module):

    def __init__(self, d_model, n_head, head_size, vocab_size, dropout, block_size, device, mask=True):

        super().__init__()

        self.heads = nn.ModuleList(
            [Head(block_size, d_model, head_size, dropout, device, mask=True) for _ in range(n_head)]
        )

        self.proj = nn.Linear(
            in_features=head_size * n_head,
            out_features=d_model,
            device=device
        )
        self.dropout = nn.Dropout(p=dropout)

    def forward(self, x, context=None):
        out = torch.cat([h(x, context) for h in self.heads], dim=-1)
        out = self.dropout(self.proj(out))
        return out

