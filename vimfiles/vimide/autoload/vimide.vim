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
" Script Variables:
" ----------------------------------------------------------------------------

" default server informations.
let s:server_host = "localhost"
let s:server_port = "3333"

" default command declartions.
let s:command_ping = "/ping"

" default library informations.
if has('win32') || has('win64') || has('win32unix')
  let s:libfilename = 'libxget.dll'
else
  let s:libfilename = 'libxget.so'
endif

" ------------------------------------------------------------------------------
" Script Functions:
" ------------------------------------------------------------------------------

" Determines the location for libxget.so/libxget.dll
function! s:GetLibXgetPath()
  " find the libxget.so/libxget.dll in runtimpath or LD_LIBRARY_PATH or Env.
  if !exists('g:VIde_LibXget') || g:VIde_LibXget == ''
    if exists("$VIDE_LIBXGET") && $VIDE_LIBXGET != '' " find with Env first.
      let g:VIde_LibXget = $VIDE_LIBXGET
    elseif (has('unix') || has('win32unix')) && exists("$LD_LIBRARY_PATH")
          \ && $LD_LIBRARY_PATH != ''
      let arr = split($LD_LIBRARY_PATH, ':')
      if len(arr) > 0
        for a in arr
          let file = findfile(s:libfilename, a)
          if file != ''
            let g:VIde_LibXget = a . g:VIdeSeparator . file
            break
          endif
        endfor
      endif
    else " find with runtimepath.
      let arr = split(&runtimepath, ',')
      if len(arr) > 0
        for a in arr
          let file = findfile(s:libfilename, a)
          if file != ''
            if file =~ a
              let g:VIde_LibXget = file
            elseif file =~ s:libfilename
              let g:VIde_LibXget = a . g:VIdeSeparator . file
            endif
            break
          endif
        endfor
      endif
    endif
  endif
  return g:VIde_LibXget
endfunction

" ----------------------------------------------------------------------------
" Functions:
" ----------------------------------------------------------------------------

" Ping the vimide server for alive determines.
function! vimide#Ping()
  let result = vimide#Execute(s:command_ping, {'raw': 0})

  if type(result) == g:STRING_TYPE
    call vimide#print#Echo(result)
  elseif type(result) == g:DICT_TYPE
    let ev = get(result, 'Eclipse')
    let vv = get(result, 'Vimide')
    let str = "Eclipse: " . ev . "\n"
    let str .= "Vimide : " . vv
    call vimide#print#EchoInfo(str)
  endif
endfunction

" Executes the command by server.
function! vimide#Execute(command, ...)
  " Optional args:
  "   options: {
  "     exec: 1 to execute the command using execute instead of system.
  "     raw:  1 to get the result without evaluating as json.
  "   }

  if g:VIdeDisable
    return 0
  endif

  let options = len(a:000) > 0 ? a:000[0] : {}
  let exec = get(options, 'exec', 0)

  let result = ''
  if a:command != ''
    let libfile = s:GetLibXgetPath()
    if libfile != ''
      let command = 'http://' . s:server_host . ':' . s:server_port 
      if a:command !~ '^/'
        let command .= '/'
      endif
      let command .= a:command

      " escape specific characters.
      let command = escape(command, '&%!')
      let command = escape(command, '%!')
      let command = escape(command, '#')
      silent! let result = libcall(libfile, 'xget', command)
    endif
  endif

  let result = substitute(result, '\n$', '', '') " removed the tail \n.
  if has('win32') || has('win64') || has('win32unix')
    let result = substitute(result, '\<c-m>$', '', '')
  endif

  " TODO: error determines here.

  let raw = get(options, 'raw', 0)
  return result != '' && !raw ? eval(result) : result
endfunction

" vim:ft=vim
