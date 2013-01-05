" Author: keyhom (keyhom.c@gmail.com)
" Copyright: 
"

" Ping the vimide server for alive determines.
function! vimide#Ping() "{{{
  let result = vimide#Execute(s:command_ping)
  if type(result) == g:STRING_TYPE
    call vimide#util#Echo(result)
  endif
endfunction " }}}



