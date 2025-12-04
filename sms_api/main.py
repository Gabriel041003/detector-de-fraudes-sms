# main.py
from fastapi import FastAPI
from pydantic import BaseModel
import pickle, re, os

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.join(BASE_DIR, "modelo_sms.pkl")

try:
    with open(MODEL_PATH, "rb") as f:
        model, vectorizer = pickle.load(f)
except Exception as e:
    print(f"Erro ao carregar o modelo: {e}")
    model, vectorizer = None, None

app = FastAPI()

class SMS(BaseModel):
    texto: str  # 🔧 padronizado como "texto"

def preprocess(t: str) -> str:
    t = t.lower()
    t = re.sub(r'\W', ' ', t)
    return t

@app.post("/classificar")
def classificar_sms(sms: SMS):
    if model is None or vectorizer is None:
        return {"erro": "Modelo ou vectorizer não carregado"}

    try:
        texto = preprocess(sms.texto)
        X = vectorizer.transform([texto])
        prob = model.predict_proba(X)[0][1]
        classe = "fraude" if prob >= 0.5 else "legitimo"
        return {"classe": classe, "probabilidade": float(prob)}
    except Exception as e:
        return {"erro": f"Falha na predição: {e}"}
