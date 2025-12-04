# treina_modelo.py
from sklearn.linear_model import LogisticRegression
from sklearn.feature_extraction.text import CountVectorizer
import pickle
import re

# 🔧 dados simples só pra começar; ideal depois usar um dataset maior (UCI/Kaggle)
X = [
    "Você ganhou um prêmio! Clique aqui.",
    "Sua conta foi bloqueada, acesse o link.",
    "Oi, tudo bem? Vamos almoçar hoje?",
    "Promoção imperdível, acesse já!",
    "Olá, segue o relatório solicitado.",
    "Seu cartão foi clonado, acesse o site para desbloquear."
]
y = [1, 1, 0, 1, 0, 1]  # 1 = fraude, 0 = legítimo

def preprocess(t: str) -> str:
    t = t.lower()
    t = re.sub(r'\W', ' ', t)
    return t

X = [preprocess(t) for t in X]

vectorizer = CountVectorizer()
X_vec = vectorizer.fit_transform(X)

model = LogisticRegression()
model.fit(X_vec, y)

with open("modelo_sms.pkl", "wb") as f:
    pickle.dump((model, vectorizer), f)

print("✅ Modelo salvo em modelo_sms.pkl")
