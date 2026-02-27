from django.db import models
from django.conf import settings

class Book(models.Model):
    CATEGORY_CHOICES = [
        ('TECH', 'School of Technology'),
        ('EDU', 'School of Education'),
        ('BIZ', 'School of Business'),
    ]

    title = models.CharField(max_length=200)
    author = models.CharField(max_length=200)
    category = models.CharField(max_length=10, choices=CATEGORY_CHOICES)
    pdf_file = models.FileField(upload_to='library/books/')
    cover_image = models.ImageField(upload_to='library/covers/', null=True, blank=True)
    uploaded_at = models.DateTimeField(auto_now_add=True)
    
    # Optional: Track who uploaded it
    uploaded_by = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.SET_NULL, null=True, blank=True)

    def __str__(self):
        return self.title

    class Meta:
        ordering = ['-uploaded_at']
