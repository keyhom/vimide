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

" ----------------------------------------------------------------------------
"
" Functions:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Makes the supplied path to a legal format, specify to cygwin.
"
" LegalPath:
"   path  - the specific path to make legal, specify to cygwin.
" ----------------------------------------------------------------------------
function! vimide#util#LegalPath(path)
  let path = a:path
  if has('win32unix') && a:path =~ '^/cygdrive'
    let path = substitute(a:path, '^/cygdrive/', '', '')
    let path = substitute(path, '^\(\w\)/', '\1:/', '')
  endif

  return path
endfunction

" ----------------------------------------------------------------------------
" Execute a command without the specific autocommands.
"
" ExecWithoutAutocmds:
"   cmd - the command to execute.
"   ... - the events to disable.
" ----------------------------------------------------------------------------
function! vimide#util#ExecWithoutAutocmds(cmd, ...)
  let save_opt = &eventignore
  " disabled the supplied autocommands first.
  let events = len(a:000) == 0 ? 'all' : a:000[0]
  exec 'set eventignore=' . events
  try
    exec a:cmd
  finally
    let &eventignore = save_opt
  endtry
endfunction

" ----------------------------------------------------------------------------
" Pad the supplied string.
"
" Pad:
"   string  - the supplied string.
"   length  - the length to format.
"   ...     - the character to pad.
" ----------------------------------------------------------------------------
function! vimide#util#Pad(string, length, ...)
  let char = a:0 > 0 ? a:0 : ' '
  let string = a:string
  while strlen(string) < a:length
    let string .= char
  endwhile
  return string
endfunction

" vim:ft=vim
