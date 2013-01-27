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

let s:command_list_install_vms = "/java_list_vms"

" ----------------------------------------------------------------------------
"
" Autoload Functions:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Lists all installed JVM.
"
" ListInstallVMs:
" ----------------------------------------------------------------------------
function! vimide#java#misc#ListInstallVMs()
  let command = s:command_list_install_vms
  let result = vimide#Execute(command)
  if type(result) == g:LIST_TYPE
    let messages = ''
    let no = 1
    for row in result
      let messages .= '[' . no . '] ' . row.name
      if row.default
        let messages .= ' [DEFAULT]'
      endif
      let messages .= "\n"
      let messages .= '    id:    ' . row.id . "\n"
      let messages .= '    type:  ' . row.type . "\n"
      let messages .= '    dir:   ' . row.dir . "\n"
      let messages .= '    args:  ' . row.args . "\n"
      let messages .= "\n"
      let no = no + 1
    endfor

    call vimide#print#Echo(messages)
  endif
endfunction

" vim:ft=vim
