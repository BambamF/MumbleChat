#!usr/bin/python3

import torch
from torch import nn
from torch.utils.data import Dataset
import json

class MovieDialogueDataset(Dataset):
    
    def __init__(self, device, encoded_pairs, train_set=True):
        super().__init__()

        self.dataset = json.load(open(encoded_pairs))

        self.train_set = train_set

        train_split: float = int(len(self.dataset) * 0.9)

        self.train_dataset = self.dataset[:train_split]
        self.test_dataset = self.dataset[train_split:]

        self.dataset_length = len(self.train_dataset) if train_set else len(self.test_dataset)

        self.device = device

    def __getitem__(self, idx: int):
        if self.train_set:
            train_pair = self.train_dataset[idx]

            encoder_input = torch.tensor(
                data=train_pair[0],
                dtype=torch.long,
                device=self.device
            )

            decoder_data = torch.tensor(
                data=train_pair[1],
                dtype=torch.long,
                device=self.device
            )

            decoder_input = decoder_data[:-1]
            decoder_output = decoder_data[1:]

            return encoder_input, decoder_input, decoder_output
        else:
            test_pair = self.test_dataset[idx]

            encoder_input = torch.tensor(
                data=test_pair[0],
                dtype=torch.long,
                device=self.device
            )

            decoder_data = torch.tensor(
                data=test_pair[1],
                dtype=torch.long,
                device=self.device
            )

            decoder_input = decoder_data[:-1]
            decoder_output = decoder_data[1:]

            return encoder_input, decoder_input, decoder_output
    
    def __len__(self):
        return self.dataset_length