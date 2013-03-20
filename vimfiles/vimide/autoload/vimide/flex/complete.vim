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

if !exists('g:VIdeFlexCompleteLayout')
  if &completeopt !~ 'preview' && &completeopt =~ 'menu'
    let g:VIdeFlexCompleteLayout = 'standard'
  else
    let g:VIdeFlexCompleteLayout = 'compact'
  endif

  if !exists('g:VIdeFlexCompleteCaseSensitive')
    let g:VIdeFlexCompleteCaseSensitive = !&ignorecase
  endif
endif

" ----------------------------------------------------------------------------
"
" Script Variables:
"
" ----------------------------------------------------------------------------

let s:command_complete = '/flexComplete?project=<project>&file=<file>&offset=<offset>&layout=<layout>'

" ----------------------------------------------------------------------------
"
" Autocmd Functions:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Handles flex code completion.
"
" CodeComplete:
"   findstart - 
"   base      -
" ----------------------------------------------------------------------------
function! vimide#flex#complete#CodeComplete(findstart, base)
  if !vimide#project#impl#IsCurrentFileInProject()
    return a:findstart ? -1 : []
  endif

  if a:findstart
    write!

    " locate the start of the word.
    let line = getline('.')
    let start = col('.') - 1

    " exceptions that break the rule.
    if line[start] == '.' && line[start - 1] != '.'
      let start -= 1
    endif

    return start
  else
    " call vimide#lang#SilentUpdate()
    write!

    let file = expand('%:p')
    let project = vimide#project#impl#GetProject()
    let offset = vimide#util#GetOffset() + len(a:base)

    if '' == file
      return []
    endif

    let command = s:command_complete
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', vimide#util#LegalPath(file, 2), '')
    let command = substitute(command, '<offset>', offset, '')
    let command = substitute(command, '<layout>', g:VIdeFlexCompleteLayout, '')

    let completions = []
    let result = vimide#Execute(command)
    if type(result) != g:DICT_TYPE
      return
    endif

    if has_key(result, 'imports') && len(result.imports)
      let imports = result.imports
      let func = 'vimide#flex#complete#ImportThenComplete(' . string(imports) . ')'
      call feedkeys("\<c-e>\<c-r>=" . func . "\<cr>", 'n')
      " prevents supertab's completion chain from attampting the next
      " completion in the chain.
      return -1
    endif

    if has_key(result, 'error') && len(result.completions) == 0
      call vimide#print#EchoError(result.error.message)
      return -1
    endif

    return completions
  endif
  return []
endfunction


" vim:ft=vim
