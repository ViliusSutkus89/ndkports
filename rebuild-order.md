#### Ports without dependencies:

- fribidi
- graphite2
- libexpat
- libffi
- libiconv
- libjpeg-turbo
- libpng
- libtool
- libuninameslist
- openjpeg
- openlibm
- pcre2
- pixman
- proxy-libintl
- spiro

#### Ports with dependencies:
- libtiff (depends on: libjpeg-turbo)
- freetype (depends on: libpng)
- glib2 (depends on: libiconv, proxy-libintl, libffi, pcre2)
- libxml2 (depends on: libiconv)

- lcms2 (depends on: libtiff)
- fontconfig (depends on: freetype, libexpat)

- cairo (depends on: fontconfig, glib2, pixman)

- harfbuzz (depends on: cairo)

- pango (depends on: harfbuzz, fribidi)

- fontforge (depends on: libtool, libuninameslist, libxml2, spiro, pango, libtiff, openlibm)
- poppler (depends on: cairo, openjpeg, lcms2)

- pdf2htmlEX (depends on: fontforge, poppler)

