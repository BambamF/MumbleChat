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
from GPT_MODEL_6 import GPT_MODEL_6
import random

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
    batch_size = 32
    max_len = 16
    dropout=0.2

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

    steps = 50

    # instantiate the model
    model = GPT_MODEL_6(
        d_model=d_model, 
        n_head=n_head, 
        n_layer=n_layer, 
        vocab=word_map, 
        reverse_vocab=reverse_word_map,
        dropout=dropout,
        block_size=max_len,
        device=device,
        max_len=max_len,
        )
    
    questions = [
        "Hi, what's your name?",
        "Please come with me, I'm scared.",
        "May the best man win.",
        "With great power...",
        "Bring that torch with you.",
        "Good Morning."
    ]

    question = random.choice(questions)
    
    sample_encoder_input = torch.tensor(
        data=[word_map.get(w, word_map['<unk>']) for w in remove_punctuation(question).strip().split()[:max_len]]
    )

    print(f"Question:\n{question}\nAnswer:")
    # test run
    with torch.no_grad():
        print(model.generate(sample_encoder_input, max_new_tokens=15))

    # set the seed
    torch.manual_seed(42)
    torch.cuda.manual_seed(42)

    LR = 1e-3
    WD = 0.01

    optimiser = torch.optim.AdamW(params=model.parameters(), lr=LR, weight_decay=WD)
    scheduler = torch.optim.lr_scheduler.StepLR(
        optimizer=optimiser,
        step_size=5,
        gamma=0.5
    )

    train_loss_list = []
    train_loss = 0

    val_loss_list = []
    val_loss = 0

    # set the model to train mode
    model.train()
    for step in tqdm(range(steps)):

        # reset the loss
        train_loss = 0
        val_loss = 0

        for batch, (encoder_input, decoder_input, decoder_output) in (enumerate(train_dataloader)):

            encoder_input = encoder_input.to(device)
            decoder_input = decoder_input.to(device)
            decoder_output = decoder_output.to(device)

            # forward pass
            curr_train_logits, curr_train_loss = model(encoder_input, decoder_input, decoder_output)

            # optimiser zero grad
            optimiser.zero_grad()

            # loss backward
            curr_train_loss.backward()

            # optimiser step
            optimiser.step()

            train_loss += curr_train_loss.item()

            if batch % 1000 == 0:
                print(f"Train Step: {step}\nBatch: {batch}\nTrain Loss: {curr_train_loss.item()}\n")

        train_loss /= len(train_dataloader)
        train_loss_list.append(train_loss)

        # set the model to eval mode
        model.eval()

        with torch.no_grad():    
            for val_batch, (val_enc_input, val_dec_input, val_dec_output) in enumerate(test_dataloader):

                val_enc_input = val_enc_input.to(device)
                val_dec_input = val_dec_input.to(device)
                val_dec_output = val_dec_output.to(device)

                curr_val_logits, curr_val_loss = model(val_enc_input, val_dec_input, val_dec_output)

                val_loss += curr_val_loss.item()

                if val_batch % 100 == 0:
                    val_question = random.choice(questions)
                    val_q_input = torch.tensor(
                        data=[word_map.get(w, word_map['<unk>']) for w in remove_punctuation(val_question).strip().split() if w],
                        dtype=torch.long,
                        device=device
                    )
                    print(f"Val Batch: {val_batch}\nVal Loss: {curr_val_loss}")

            val_loss /= len(test_dataloader)
            val_loss_list.append(val_loss)

        # scheduler step
        scheduler.step()

        if step % 5 == 0:
            val_question = random.choice(questions)
            val_q_input = torch.tensor(
                data=[word_map.get(w, word_map['<unk>']) for w in remove_punctuation(val_question).strip().split() if w],
                dtype=torch.long,
                device=device,
                requires_grad=False
            )

            with torch.inference_mode():
                print(f"Question:\n{val_question}\nAnswer:\n{model.generate(val_q_input, max_new_tokens=max_len)}")

if __name__ == "__main__":
    __main__()