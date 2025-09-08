#!usr/bin/python3

import torch
from torch import nn
from torch.nn import functional as F
from torch.utils.data import Dataset
from torch.utils.data import DataLoader
from tqdm.auto import tqdm
from collections import Counter
from timeit import default_timer as timer
import kagglehub
import os
import re
import traceback
import json
from shutil import rmtree, copytree
from MovieDialogueDataset import MovieDialogueDataset

def remove_punctuation(sentence: str) -> str:
    pattern = r"[^a-zA-Z0-9\s]"
    return re.sub(pattern, "", sentence)

def add_to_word_freq(word_freq: Counter, sentence):
    cleaned = [w for w in sentence if w]
    word_freq.update(cleaned)

def word_to_idx(words: list[str], word_map: dict[str, int]) -> list[int]:
    return [word_map.get(w, word_map['<unk>']) for w in words]

def idx_to_word(idx: list[int], reverse_word_map: dict[int, str]) -> list[str]:
    return [reverse_word_map.get(i, reverse_word_map[16577]) for i in idx]

def __main__():
    # device agnostic
    device = "cuda" if torch.cuda.is_available() else "cpu"

    RELATIVE_PATH = os.getcwd()

    max_len = 15

    # set up environment
    MODELS_DIR = os.path.join(RELATIVE_PATH, "src/bot/models")
    DATA_DIR = os.path.join(RELATIVE_PATH, "src/bot/data")
    kagglehub_dir = os.path.join(RELATIVE_PATH, "src/bot/data/kagglehub")

    if not os.path.isdir(MODELS_DIR):
        print("Creating models directory")
        os.makedirs(MODELS_DIR)
        print(f"Models directory created at: {MODELS_DIR}")
    else:
        print("Models directory already exists")

    if not os.path.isdir(DATA_DIR):
        print("Creating data directory")
        os.makedirs(DATA_DIR)
        print(f"Data directory created at: {DATA_DIR}")
    else:
        print("Data directory already exists")

    if not os.path.isdir(kagglehub_dir):
        print("Creating kagglehub directory")
        os.makedirs(kagglehub_dir)
        print(f"Kagglehub directory created at: {kagglehub_dir}")
    else:
        print("Kagglehub directory already exists")

    kh_dir_content = os.listdir(kagglehub_dir)

    if len(kh_dir_content) < 1:
        data_import = kagglehub.dataset_download("rajathmc/cornell-moviedialog-corpus")

        print(f"Imported kagglehub data path: {data_import}")
        copytree(data_import, kagglehub_dir, dirs_exist_ok=True)
        print(f"Dataset copied from {data_import} to {kagglehub_dir}")
    
    lines_path = os.path.join(kagglehub_dir, "movie_lines.txt")
    conv_path = os.path.join(kagglehub_dir, "movie_conversations.txt")

    if not os.path.isfile(lines_path):
        raise FileNotFoundError("Movie lines file not found")

    if not os.path.isfile(conv_path):
        raise FileNotFoundError("Movie conversations file not found")
    

    with open(lines_path, "r", encoding='ISO-8859-1') as l_f:
        lines_text = l_f.readlines()

    with open(conv_path, "r") as c_f:
        conv_text = c_f.readlines()

    lines_map = {}

    for line in lines_text:
        split_line = line.strip().split(" +++$+++ ")
        lines_map[split_line[0]] = split_line[-1]

    conv_pairs = []

    for conv in conv_text:
        try:
            split_conv = conv.strip().split(" +++$+++ ")
            conv_list = eval(split_conv[-1])
            for i, conv in enumerate(conv_list):
                if i < len(conv_list) - 1:
                    conv_pairs.append([remove_punctuation(lines_map[conv]).strip().split()[:max_len], remove_punctuation(lines_map[conv_list[i + 1]]).strip().split()[:max_len]])
        except Exception as e:
            print(f"Exception: {e} | Conv: {conv.split(" +++$+++ ")[-1]}")
            traceback.print_exception
            break

    word_freq = Counter()

    left = 0
    right = len(conv_pairs) - 1

    while left <= right:
        if left < right:
            add_to_word_freq(word_freq, conv_pairs[left][0])
            add_to_word_freq(word_freq, conv_pairs[left][1])
            add_to_word_freq(word_freq, conv_pairs[right][0])
            add_to_word_freq(word_freq, conv_pairs[right][1])
        else:
            add_to_word_freq(word_freq, conv_pairs[left][0])
            add_to_word_freq(word_freq, conv_pairs[left][1])
        left += 1
        right -= 1
    
    word_map = {v:i+1 for i,v in enumerate(word_freq.keys()) if word_freq[v] > 6}

    word_map['<pad>'] = 0
    word_map['<unk>'] = len(word_map)
    word_map['<start>'] = len(word_map)
    word_map['<end>'] = len(word_map)


    reverse_word_map = {v: k for k, v in word_map.items()}

    BOT_DIR = os.path.join(RELATIVE_PATH, "src/bot")

    word_map_path = os.path.join(BOT_DIR, "WORD_MAP.json")
    reverse_word_map_path = os.path.join(BOT_DIR, "REVERSE_WORD_MAP.json")

    with open(word_map_path, "w") as w_map:
        json.dump(word_map, w_map)

    with open(reverse_word_map_path, "w") as r_w_map:
        json.dump(reverse_word_map, r_w_map)
    
    encoded_conv_pairs = [[word_to_idx(i, word_map), word_to_idx(j, word_map)] for [i, j] in conv_pairs]

    conv_pairs_path = os.path.join(BOT_DIR, "PAIRS.json")
    encoded_pairs_path = os.path.join(BOT_DIR, "ENCODED_PAIRS.json")

    with open(conv_pairs_path, "w") as con_p:
        json.dump(conv_pairs, con_p)
    
    with open(encoded_pairs_path, "w") as enc_p:
        json.dump(encoded_conv_pairs, enc_p)
    
    train_dataset = MovieDialogueDataset(device, encoded_pairs_path, train_set=True)
    test_dataset = MovieDialogueDataset(device, encoded_pairs_path, train_set=False)

    d_model = 512
    n_head = 8
    n_layer = 6
    block_size = 15
    batch_size = 32
    max_len = 15

    train_dataloader = DataLoader(
        dataset=train_dataset,
        batch_size=batch_size,
        shuffle=True
    )

    test_dataloader = DataLoader(
        dataset=test_dataset,
        batch_size=batch_size,
        shuffle=False
    )

    print(len(train_dataloader))
    print(len(test_dataloader))
    

if __name__ == "__main__":
    __main__()