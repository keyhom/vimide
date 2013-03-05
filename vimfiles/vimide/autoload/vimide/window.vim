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
" Global Variables:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
"
" Script Variables:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
"
" Autocmd Functions:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Opens a temp window w/ the given name and contents which is readonly unless
" specified otherwise.
"
" TempWindow:
"   name      - the name of the temp window.
"   lines     - the lines to display.
"   [options] - the other optional options.
" ----------------------------------------------------------------------------
function! vimide#window#TempWindow(name, lines, ...)
  let options = a:0 > 0 ? a:1 : {}
  let filename = expand('%:p')
  let winnr = winnr()

  let bufname = vimide#util#EscapeBufferName(a:name)
  let name = escape(a:name, ' ')
  if has('unix')
    let name = escape(name, '[]')
  endif

  let line = 1
  let col = 1

  if bufwinnr(bufname) == -1
    let height = get(options, 'height', 10)
    silent! noautocmd exec 'botright ' . height . 'sview ' . name
    setlocal nowrap
    setlocal winfixheight
    setlocal noswapfile
    setlocal nobuflisted
    setlocal buftype=nofile
    setlocal bufhidden=delete
    silent doautocmd WinEnter
  else
    let temp_winnr = bufwinnr(bufname)
    if temp_winnr != winnr()
      exec temp_winnr . 'winc w'
      silent doautocmd WinEnter
      if get(options, 'preserveCursor', 0)
        let line = line('.')
        let col = col('.')
      endif
    endif
  endif

  call vimide#window#TempWindowClear(a:name)

  setlocal modifiable
  setlocal noreadonly
  call append(1, a:lines)
  retab
  silent 1,1delete _

  call cursor(line, col)

  if get(options, 'readonly', 1)
    setlocal nomodified
    setlocal nomodifiable
    setlocal readonly
  endif

  silent doautocmd BufEnter

  " Store filename and window number so that plugins can use it if necessary.
  if filename != expand('%:p')
    let b:filename = filename
    let b:winnr = winnr

    augroup vimide_temp_window
      autocmd! BufWinLeave <buffer>
      call vimide#util#GoToBufferWindowRegister(b:filename)
    augroup END
  endif
endfunction

" ----------------------------------------------------------------------------
" Clears the contents of the temp window with the given name.
"
" TempWindowClear:
"   name  - the name of the temp window.
" ----------------------------------------------------------------------------
function! vimide#window#TempWindowClear(name)
  let name = vimide#util#EscapeBufferName(a:name)
  if bufwinnr(name) != -1
    let curwinnr = winnr()
    exec bufwinnr(name) . 'winc w'
    setlocal modifiable
    setlocal noreadonly
    silent 1,$delete _
    exec curwinnr . 'winc w'
  endif
endfunction

" vim:ft=vim
