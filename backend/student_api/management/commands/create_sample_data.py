from django.core.management.base import BaseCommand
from student_api.sample_data import create_all_sample_data

class Command(BaseCommand):
    help = 'Create sample data for testing'

    def handle(self, *args, **options):
        self.stdout.write('Creating sample data...')
        create_all_sample_data()
        self.stdout.write(
            self.style.SUCCESS('Successfully created sample data')
        )