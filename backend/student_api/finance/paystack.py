import requests
from django.conf import settings
import logging

logger = logging.getLogger(__name__)

class PaystackClient:
    def __init__(self):
        self.secret_key = getattr(settings, 'PAYSTACK_SECRET_KEY', 'PLACEHOLDER_PAYSTACK_SECRET')
        self.base_url = 'https://api.paystack.co'
        self.headers = {
            'Authorization': f'Bearer {self.secret_key}',
            'Content-Type': 'application/json',
        }

    def initialize_transaction(self, email, amount, reference, callback_url=None):
        """
        Initialize a transaction. For PesaLink/Bank Transfer, we'll suggest 'bank_transfer' channel.
        """
        url = f"{self.base_url}/transaction/initialize"
        data = {
            "email": email,
            "amount": int(amount * 100), # Paystack expects amount in cents/kobo
            "reference": reference,
            "callback_url": callback_url or getattr(settings, 'PAYSTACK_CALLBACK_URL', None),
            "channels": ["bank_transfer", "mobile_money", "card"] # Enable multiple including PesaLink
        }
        
        try:
            response = requests.post(url, headers=self.headers, json=data)
            response_data = response.json()
            if response.status_code == 200 and response_data.get('status'):
                return response_data.get('data')
            else:
                logger.error(f"Paystack init failed: {response_data}")
                return None
        except Exception as e:
            logger.exception("Paystack init exception")
            return None

    def verify_transaction(self, reference):
        url = f"{self.base_url}/transaction/verify/{reference}"
        try:
            response = requests.get(url, headers=self.headers)
            response_data = response.json()
            if response.status_code == 200 and response_data.get('status'):
                return response_data.get('data')
            return None
        except Exception as e:
            logger.exception("Paystack verify exception")
            return None

    def verify_webhook_signature(self, payload, signature):
        """
        Verify the signature of a Paystack webhook.
        """
        import hmac
        import hashlib
        
        computed_signature = hmac.new(
            self.secret_key.encode('utf-8'),
            payload,
            hashlib.sha512
        ).hexdigest()
        
        return computed_signature == signature
