import torch
from torch import nn
from torch.nn import functional as F

class Head(nn.Module):

    def __init__(self, block_size, d_model, head_size, dropout, device, mask=True):

        super().__init__()

        self.query = nn.Linear(
            in_features=d_model,
            out_features=head_size,
            device=device
        )
        self.key = nn.Linear(
            in_features=d_model,
            out_features=head_size,
            device=device
        )
        self.value = nn.Linear(
            in_features=d_model,
            out_features=head_size,
            device=device
        )

        self.dropout = nn.Dropout(p=dropout)
        self.register_buffer('tril', torch.ones((block_size, block_size)))
        self.block_size = block_size
        self.mask = mask

    def forward(self, x, context=None):
        if context is None:
            context = x
        B, T_x, _ = x.shape
        B, T_c, _ = context.shape
        k = self.key(x)
        q = self.query(context)
        wei = q @ k.transpose(-2, -1) * (q.size(-1) ** -0.5)

        if self.mask:
            mask = self.tril[:T_x, :T_c]
            mask = mask.unsqueeze(0)
            wei = wei.masked_fill(mask == 0, float('inf'))
        
        wei = F.softmax(wei, dim=-1)
        wei = self.dropout(wei)
        v = self.value(context)

        return wei @ v

