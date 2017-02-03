try:
    from setuptools import setup
except ImportError:
    from distutils.core import setup

config = {
    'description': 'Java 2 TA',
    'author': 'Francesco Spegni',
    'url': 'TODO',
    'download_url': 'TODO',
    'author_email': 'f.spegni@univpm.it',
    'version': '0.1',
    'install_requires': ['nose'],
    'packages': ['java2ta'],
    'scripts': [],
    'name': 'projectname'
}

setup(**config)

