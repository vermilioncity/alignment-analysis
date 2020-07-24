from setuptools import setup, find_packages

setup(name='alignment_analysis',
      version='0.1.0',
      packages=find_packages(),
      entry_points={
          'console_scripts': [
              'app=alignment_analysis.commands:cli',
          ],
      },
)

