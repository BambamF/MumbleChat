import torch
from torch import nn

class MultiHead(nn.Module):

    def __init__(self, d_model, n_head, head_size, vocab_size, dropout, device, mask=True):

        super().__init__()

