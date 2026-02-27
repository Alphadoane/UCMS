from django.urls import path
from . import views

urlpatterns = [
    path('mpesa/stk-push', views.initiate_stk_push, name='initiate_stk_push'),
    path('mpesa/callback', views.mpesa_callback, name='mpesa_callback'),
    # Add other finance urls here if any, e.g. invoices
]
