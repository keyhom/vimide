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

let s:command_update_src_file = "/<lang>UpdateSrcFile?project=<project>&file=<file>"

" ----------------------------------------------------------------------------
"
" Autoload Functions:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Updates the src file on the server w/ the changes made to the current file.
"
" UpdateSrcFile:
"   lang      - the specific lang of the src file.
"   validate  - the specific flag of validate.
" ----------------------------------------------------------------------------
function! vimide#lang#UpdateSrcFile(lang, validate)
  let file = expand('%:p')
  let project = vimide#project#impl#GetProject(file)
  if '' != project
    let command = s:command_update_src_file
    let command = substitute(command, '<lang>', a:lang, '')
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')

    if a:validate
      let command .= '&validate=1'
      " if problem list wasn't empty and global prefered to update src file on
      " save.
      if vimide#project#problem#IsNotEmpty() && g:VIdeProjectProblemsUpdateOnBuild
        let command .= '&build=1'
      endif
    endif

    let result = vimide#Execute(command)

    if type(result) == g:LIST_TYPE && len(result) > 0
      " Update the quickfix list here.
      let errors = vimide#util#AssembleLocationEntries(result)
      call vimide#util#SetQuickfixList(errors)
    else
      call vimide#util#ClearQuickfixList('global')
    endif

    call vimide#project#problem#ProblemsUpdate('save')
  elseif a:validate && expand('<amatch>') == ''
    call vimide#project#impl#IsCurrentFileInProject()
  endif
endfunction

" ----------------------------------------------------------------------------
" Silently updates the current source file w/out validation.
"
" SilentUpdate: 
"   [temp]
"   [temp_write]
" ----------------------------------------------------------------------------
function! vimide#lang#SilentUpdate(...)
  let pos = getpos('.')
  let file = expand('%:p')
  if file != ''
    try 
      if a:0 && a:1
        " don't create temp files if no server if available to clean them up.
        let project = vimide#project#impl#GetProject(file)
      elseif a:0 < 2 || a:2
        silent noautocmd update
      endif
    finally
      call setpos('.', pos)
    endtry
  endif
  return vimide#util#LegalPath(file)
endfunction

" vim:ft=vim
