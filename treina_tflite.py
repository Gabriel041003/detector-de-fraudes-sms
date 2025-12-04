import json
import re
import numpy as np
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras.layers import Dense, Embedding, GlobalAveragePooling1D
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences

# ====== 1) Dados EXEMPLO (depois troque por um dataset maior!) ======
sms = [
    "Você ganhou um prêmio! Clique aqui.",
    "Sua conta foi bloqueada, acesse o link.",
    "Oi, tudo bem? Vamos almoçar hoje?",
    "Promoção imperdível, acesse já!",
    "Olá, segue o relatório solicitado.",
    "Seu cartão foi clonado, acesse o site para desbloquear."
]
labels = np.array([1, 1, 0, 1, 0, 1])  # 1 = fraude, 0 = legítimo

def preprocess(t: str) -> str:
    t = t.lower()
    t = re.sub(r'[^a-zà-ú0-9 ]', ' ', t)
    t = re.sub(r'\s+', ' ', t).strip()
    return t

sms = [preprocess(t) for t in sms]

# ====== 2) Tokenização ======
NUM_WORDS = 1000
MAX_LEN = 20
OOV = "<OOV>"

tokenizer = Tokenizer(num_words=NUM_WORDS, oov_token=OOV)
tokenizer.fit_on_texts(sms)
seqs = tokenizer.texts_to_sequences(sms)
X = pad_sequences(seqs, maxlen=MAX_LEN)  # padding='pre' (à esquerda)

# ====== 3) Modelo simples ======
model = keras.Sequential([
    Embedding(NUM_WORDS, 16, input_length=MAX_LEN),
    GlobalAveragePooling1D(),
    Dense(16, activation="relu"),
    Dense(1, activation="sigmoid")
])
model.compile(optimizer="adam", loss="binary_crossentropy", metrics=["accuracy"])
model.fit(X, labels, epochs=30, verbose=0)

# ====== 4) Exportar para TFLite ======
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()
with open("modelo_sms.tflite", "wb") as f:
    f.write(tflite_model)

# ====== 5) Exportar vocabulário simples ======
vocab = {
    "word_index": tokenizer.word_index,            # {"palavra": indice}
    "oov_index": tokenizer.word_index.get(OOV, 1), # normalmente 1
    "max_len": MAX_LEN,
    "num_words": NUM_WORDS
}
with open("vocab.json", "w", encoding="utf-8") as f:
    json.dump(vocab, f, ensure_ascii=False)

print("✅ Gerados: modelo_sms.tflite e vocab.json")
