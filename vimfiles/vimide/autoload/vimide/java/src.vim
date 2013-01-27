" Vimide Script.
" Author: keyhom (keyhom.c@gmail.com)
" License: Copyright (c) 2012 keyhom.c@gmail.com.
" 
"   This software is provided 'as-is', without any express or implied warranty.
"   In no event will the authors be held liable for any damages arising from
"   the use of this software.
"   
"   Permission is granted to anyone to use this software for any purpose
"   excluding commercial applications, and to alter it and redistribute it
"   freely, subject to the following restrictions:
"   
"     1. The origin of this software must not be misrepresented; you must not
"     claim that you wrote the original software. If you use this software
"     in a product, an acknowledgment in the product documentation would be
"     appreciated but is not required.
"   
"     2. Altered source versions must be plainly marked as such, and must not
"     be misrepresented as being the original software.
"   
"     3. This notice may not be removed or altered from any source
"     distribution.
"

" ----------------------------------------------------------------------------
"
" Script Variables:
"
" ----------------------------------------------------------------------------

let s:command_java_format = "/java_src_format?project=<project>&file=<file>&hoffset=<hoffset>&toffset=<toffset>"

" ----------------------------------------------------------------------------
"
" Autoload Functions:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Formats the supplied src file.
"
" Format:
"   first - the start line of the src file.
"   last  - the end line of the src file.
" ----------------------------------------------------------------------------
function! vimide#java#src#Format(first, last)
  let file = expand('%:p')
  let project = vimide#project#impl#GetProject(file)

  if '' == project
    return
  endif

  " current the file location.
  let file = vimide#util#LegalPath(file)

  " save the file to supply the dirty commit.
  write

  " silent updated.
  " get relative file path.
  let command = s:command_java_format
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let begin = vimide#util#GetOffset(a:first, 1)
  let end = vimide#util#GetOffset(a:last, 1) + len(getline(a:last)) - 1
  let command = substitute(command, '<hoffset>', begin, '')
  let command = substitute(command, '<toffset>', end, '')

  let result = vimide#Execute(command)
  if result != '0'
    call vimide#util#Reload({'retab': 1})
    write
  endif
endfunction

" vim:ft=vim
