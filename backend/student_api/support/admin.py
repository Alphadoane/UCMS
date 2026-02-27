from django.contrib import admin
from .models import Complaint, ComplaintAttachment, ComplaintComment, ComplaintTimeline

class ComplaintAttachmentInline(admin.TabularInline):
    model = ComplaintAttachment
    extra = 0
    readonly_fields = ('uploaded_at',)

class ComplaintCommentInline(admin.TabularInline):
    model = ComplaintComment
    extra = 1
    readonly_fields = ('created_at',)

class ComplaintTimelineInline(admin.TabularInline):
    model = ComplaintTimeline
    extra = 0
    readonly_fields = ('created_at',)

@admin.register(Complaint)
class ComplaintAdmin(admin.ModelAdmin):
    list_display = ('id', 'student', 'course', 'status', 'priority', 'created_at')
    list_filter = ('status', 'priority')
    search_fields = ('description', 'student__user__email', 'course__name')
    readonly_fields = ('created_at', 'updated_at')
    
    inlines = [ComplaintAttachmentInline, ComplaintCommentInline, ComplaintTimelineInline]

@admin.register(ComplaintComment)
class ComplaintCommentAdmin(admin.ModelAdmin):
    list_display = ('complaint', 'user', 'created_at')
    readonly_fields = ('created_at',)

@admin.register(ComplaintTimeline)
class ComplaintTimelineAdmin(admin.ModelAdmin):
    list_display = ('complaint', 'event_type', 'user', 'created_at')
    readonly_fields = ('created_at',)
