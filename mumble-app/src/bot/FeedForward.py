import torch
from torch import nn

class FeedForward(nn.Module):

    def __init__(self, d_model, dropout, device):

        super().__init__()

        self.net = nn.Sequential(
            nn.Linear(
                in_features=d_model,
                out_features=d_model*4,
                device=device
            ),
            nn.ReLU(),
            nn.Linear(
                in_features=d_model*4,
                out_features=d_model,
                device=device
            ),
            nn.ReLU(),
            nn.Dropout(p=dropout)
        )
        
    def forward(self, x):
        return self.net(x)