from rest_framework import serializers
from .models import Book

class BookSerializer(serializers.ModelSerializer):
    class Meta:
        model = Book
        fields = ['id', 'title', 'author', 'category', 'pdf_file', 'cover_image', 'uploaded_at']
        read_only_fields = ['id', 'uploaded_at']
