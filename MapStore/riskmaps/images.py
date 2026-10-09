"""
Pillow 6.2.2 is the last release for Python 2 and has known bugs in many of its
file format decoders, so only let it read the formats maps actually use.
"""
from PIL import Image

ALLOWED_FORMATS = ('PNG', 'JPEG', 'GIF')

# file signatures of the allowed formats, checked before Pillow sees the file
MAGIC_BYTES = (
    '\x89PNG\r\n\x1a\n',
    '\xff\xd8\xff',
    'GIF87a',
    'GIF89a',
)


def restrict_pillow_formats():
    # preinit() registers only a few common formats, init() would register all the rest,
    # so register the common ones, drop any we do not allow, and mark Pillow as fully
    # initialised so init() never loads the other plugins
    Image.preinit()
    for image_format in list(Image.OPEN.keys()):
        if image_format not in ALLOWED_FORMATS:
            del Image.OPEN[image_format]
    Image.ID[:] = [image_format for image_format in Image.ID if image_format in ALLOWED_FORMATS]
    Image._initialized = 2


def is_allowed_image(path):
    f = open(path, 'rb')
    try:
        header = f.read(8)
    finally:
        f.close()
    return any(header.startswith(magic) for magic in MAGIC_BYTES)
