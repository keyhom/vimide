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

let s:command_format = "/flexFormat?project=<project>&file=<file>&hoffset=<hoffset>&toffset=<toffset>"
let s:command_import = "/flexImport?project=<project>&file=<file>&offset=<offset>"
let s:command_comment = "/flexComment?project=<project>&file=<file>&offset=<offset>"
let s:command_organize_imports = "/flexOrganizeImports?project=<project>&file=<file>&offset=<offset>"

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
function! vimide#flex#src#Format(first, last)
  if !vimide#project#impl#IsCurrentFileInProject()
    return
  endif

  call vimide#print#Echo("Formatting...")

  " silent updated.
  let file = vimide#lang#SilentUpdate()
  let project = vimide#project#impl#GetProject()

  let command = s:command_format
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
    call vimide#print#EchoInfo('Formatted.')
  else
    call vimide#print#EchoError('Illegal response result.')
  endif
endfunction

" ----------------------------------------------------------------------------
" Add/Update the comments for the elements under the cursor.
"
" Comment:
" ----------------------------------------------------------------------------
function! vimide#flex#src#Comment()
  if !vimide#project#impl#IsCurrentFileInProject()
    return
  endif

  call vimide#print#Echo('Generating...')

  let file = vimide#lang#SilentUpdate()
  let offset = vimide#util#GetCurrentElementOffset()
  let project = vimide#project#impl#GetProject()

  let command = s:command_comment
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<offset>', offset, '')

  let result = vimide#Execute(command)

  if '0' != result
    call vimide#util#Reload({'retab': 1})
    write
    call vimide#print#EchoInfo('Updated.')
  else
    call vimide#print#EchoError('Illegal response result.')
  endif

endfunction

" ----------------------------------------------------------------------------
" Organize imports by cleaning up.
"
" OrganizeImports:
" ----------------------------------------------------------------------------
function! vimide#flex#src#OrganizeImports(...)
  if !vimide#project#impl#IsCurrentFileInProject()
    return
  endif

  call vimide#print#Echo("Organizing imports...")

  let file = vimide#lang#SilentUpdate()
  let offset = vimide#util#GetCurrentElementOffset()
  let project = vimide#project#impl#GetProject()

  let command = s:command_organize_imports
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<offset>', offset, '')

  let result = vimide#Execute(command)

  if '0' == result
    call vimide#print#EchoError(command)
  elseif '1' == result
    call vimide#util#Reload({'retab': 1})
    write!
    call vimide#print#EchoInfo("Organized.")
  endif
endfunction

" vim:ft=vim
