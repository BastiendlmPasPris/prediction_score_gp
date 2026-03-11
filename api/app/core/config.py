from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    JWT_SECRET: str = "f1predict_secret_key_change_in_prod"
    DATABASE_URL: str = "sqlite:///./f1predict.db"
    ML_MODELS_PATH: str = "../ml/models/versions"
    DEBUG: bool = True

    class Config:
        env_file = ".env"


settings = Settings()
