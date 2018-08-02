try:
    from setuptools import setup, find_packages
except ImportError:
    raise ImportError("Install setup tools")

config = {
    'description': 'Java 2 TA',
    'author': 'Francesco Spegni',
    'url': 'TODO',
    'download_url': 'TODO',
    'author_email': 'f.spegni@univpm.it',
    'version': '0.1',
    'install_requires': [   
        'nose', 
        'Jinja2>=2.9.6',
        'prompt-toolkit>=1.0.15',
        'requests>=2.13.0',
        'PyContracts>=1.7.15',
        'graphviz>=0.8',
        'pydot>=1.2.4',
    ],
    'packages': find_packages(), 
    'scripts': [],
    'name': 'java2ta',
    'license': 'MIT',
}

setup(**config)


