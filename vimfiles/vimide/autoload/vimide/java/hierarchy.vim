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

if !exists('g:VIdeJavaHierarchyDefaultAction')
  let g:VIdeJavaHierarchyDefaultAction = g:VIdeDefaultFileOpenAction
endif

" ----------------------------------------------------------------------------
"
" Script Variables:
"
" ----------------------------------------------------------------------------
let s:command_hierarchy = "/javaHierarchy?project=<project>&file=<file>&offset=<offset>"

" ----------------------------------------------------------------------------
"
" Script Functions:
"
" ----------------------------------------------------------------------------
function! s:FormatHierarchy(hierarchy, lines, info, indent)
  call add(a:lines, a:indent . a:hierarchy.name)
  call add(a:info, a:hierarchy.qualified)
  let indent = vimide#util#GetIndent(1)
  for child in a:hierarchy.children
    call s:FormatHierarchy(child, a:lines, a:info, a:indent . indent)
  endfor
endfunction

function! s:Open(action)
  let line = line('.')
  if line > len(b:hierarchy_info)
    return
  endif

  let type = b:hierarchy_info[line - 1]
  " go to the buffer that initiated the hierarchy
  exec b:winnr . 'winc w'

  " source the search plugin if necessary
  if !exists('g:VIdeJavaSearchSingleResult')
    runtime autoload/vimide/java/search.vim
  endif

  let action = a:action
  let filename = expand('%:p')
  if exists('b:filename')
    let filename = b:filename
    if !vimide#util#GoToBufferWindow(b:filename)
      " if the file is no longer open, open it
      silent! exec action . ' ' . b:filename
      let action = 'edit'
    endif
  endif

  if line != 1
    let saved = g:VIdeJavaSearchSingleResult
    try
      let g:VIdeJavaSearchSingleResult = action
      if vimide#java#search#SearchAndDisplay('javaSearch', '-x declarations -p ' . type)
        let b:filename = filename
      endif
    finally
      let g:VIdeJavaSearchSingleResult = saved
    endtry
  endif
endfunction

" ----------------------------------------------------------------------------
"
" Autocmd Functions:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Represents a hierarchy resolving.
"
" Hierarchy:
" ----------------------------------------------------------------------------
function! vimide#java#hierarchy#Hierarchy()
  if !vimide#project#impl#IsCurrentFileInProject()
    return
  endif

  let project = vimide#project#impl#GetProject()
  let file = expand('%:p')
  let command = s:command_hierarchy
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', vimide#util#LegalPath(file, 2), '')
  let command = substitute(command, '<offset>', vimide#util#GetOffset(), '')
  
  let result = vimide#Execute(command)
  if type(result) != g:DICT_TYPE
    return
  endif

  let lines = []
  let info = []
  call s:FormatHierarchy(result, lines, info, '')
  call vimide#window#TempWindow('[Hierarchy]', lines)

  set ft=java
  setlocal modifiable noreadonly
  call append(line('$'), ['', '" use ? to view help'])
  setlocal nomodifiable readonly
  syntax match Comment /^".*/

  let b:hierarchy_info = info
  call vimide#print#Echo(b:hierarchy_info[line('.') - 1])

  augroup vimide_java_hierarchy
    au!
    autocmd CursorMoved <buffer>
          \ if line('.') <= len(b:hierarchy_info) |
          \   call vimide#print#Echo(b:hierarchy_info[line('.') - 1]) |
          \ else |
          \   echo '' |
          \ endif
  augroup END

  nnoremap <buffer> <silent> <cr>
        \ :call <SID>Open(g:VIdeJavaHierarchyDefaultAction)<cr>
  nnoremap <buffer> <silent> E :call <SID>Open('edit')<cr>
  nnoremap <buffer> <silent> S :call <SID>Open('split')<cr>
  nnoremap <buffer> <silent> T :call <SID>Open("tablast \| tabnew")<cr>

  " assign to buffer var to get around weird vim issue passing list containing
  " a string w/ a '<' in it on execution of mapping.
  let b:hierarchy_help = [
        \ '<cr> - open file with default action',
        \ 'E    - open with :edit',
        \ 'S    - open in a new split window',
        \ 'T    - open in a new tab',
        \ ]
  nnoremap <buffer> <silent> ?
        \ :call vimide#help#BufferHelp(b:hierarchy_help, 'vertical', 40)<cr>
endfunction

" vim:ft=vim
