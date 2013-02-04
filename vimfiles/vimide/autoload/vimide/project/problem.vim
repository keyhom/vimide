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

if !exists('g:VIdeProblemsQuickFixOpen')
  let g:VIdeProblemsQuickFixOpen = 'botright copen'
endif

" ----------------------------------------------------------------------------
"
"  Script Variables:
"
" ----------------------------------------------------------------------------

let s:command_problems = "/problems"

" ----------------------------------------------------------------------------
"
" Autoload Functions:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Performs to process problems list to quick fix list.
"
" Problems:
"   bang      - the specific charactor by process all project.
"   args[...] - the arguments withing projects and options.
" ----------------------------------------------------------------------------
function! vimide#project#problem#Problems(bang, open, ...)
  " determines the reacting projects.
  let projects = []
  if a:bang == '!'
    let projects = vimide#project#impl#GetProjectNames()
  endif

  for _arg in a:000
    if _arg !~ '^-\w' 
      silent! call add(projects, _arg)
    endif
  endfor

  if !len(projects)
    let _p = vimide#project#impl#GetProject(expand('%:p'))
    if '' != _p && vimide#project#impl#IsProjectExists(_p)
      silent! call add(projects, _p)
    endif
  endif

  for _p in projects
    if !vimide#project#impl#IsProjectExists(_p)
      silent! call remove(projects, _p)
    endif
  endfor

  if !len(projects)
    call vimide#project#impl#UnableToDetermineProject()
    return
  endif

  let command = s:command_problems
  let queryString = ''

  for _p in projects
    if len(queryString) > 0
      let queryString .= '&'
    endif
    let queryString .= 'project=' . _p
  endfor

  let command .= '?' . queryString

  let result = vimide#Execute(command)
  let errors = []
  if type(result) == g:LIST_TYPE && len(result) > 0
    let errors = vimide#util#AssembleLocationEntries(result, g:VIdeValidateSortResults)
  endif

  if len(errors) > 0
    let action = vimide#project#problem#IsNotEmpty() ? 'r' : ' '
    call vimide#util#SetQuickfixList(errors, action)

    " generate a 'signature' to distinguish the problems list from other qf
    " lists.
    let s:vide_problems_sig = s:QuickfixSignature()
    let s:vide_problems_bang = a:bang

    if a:open
      exec g:VIdeProblemsQuickFixOpen
    endif
  endif
endfunction

" ----------------------------------------------------------------------------
" Compares the problems signature against the signature of the current list to
" see if we are now have the problems list isn't empty, probably via :colder
" or :cnewer.
" ----------------------------------------------------------------------------
function! vimide#project#problem#IsNotEmpty()
  if exists('s:vide_problems_sig')
    return s:QuickfixSignature() == s:vide_problems_sig
  endif
  if exists('s:vide_problems_bang')
    unlet s:vide_problems_bang
  endif
  return 0
endfunction

function! s:QuickfixSignature()
  let qflist = getqflist()
  let len = len(qflist)
  return {
        \ 'len': len,
        \ 'first': len > 0 ? (qflist[0]['bufnr'] . ':' . qflist[0]['text']) : '',
        \ 'last': len > 0 ? (qflist[-1]['bufnr'] . ':' . qflist[-1]['text']) : '',
        \ }
endfunction

" ----------------------------------------------------------------------------
" Updates the problems which in the current quickfix list.
"
" ProblemUpdate:
"   action  - the specific action to perform the update phase.
" ----------------------------------------------------------------------------
function! vimide#project#problem#ProblemsUpdate(action)
  if a:action == 'save' && !g:VIdeProjectProblemsUpdateOnSave 
    return
  endif

  if a:action == 'build' && !g:VIdeProjectProblemsUpdateOnBuild
    return
  endif

  if !vimide#project#problem#IsNotEmpty()
    return
  endif

  " preserve the cursor position in the quickfix window.
  let qf_winnr = 0
  let index = 1
  while index <= winnr('$')
    if getbufvar(winbufnr(index), '&ft') == 'qf'
      let cur = winnr()
      let qf_winnr = index
      exec qf_winnr . 'winc w'
      let pos = getpos('.')
      exec cur . 'winc w'
      break
    endif
    let index += 1
  endwhile

  let bang = exists('s:vide_problems_bang') ? s:vide_problems_bang : ''
  call vimide#project#problem#Problems(bang, 0)

  " restore the cursor position.
  if qf_winnr
    let cur = winnr()
    exec qf_winnr . 'winc w'
    call setpos('.', pos)
    redraw
    exec cur . 'winc w'
  endif
endfunction

" ----------------------------------------------------------------------------
" Command completion for the Problems.
"
" CommandCompleteProblems:
" ----------------------------------------------------------------------------
function! vimide#project#problem#CommandCompleteProblems(argLead, cmdLine, cursorPos)
  return vimide#project#impl#CommandCompleteMultiProject(a:argLead, a:cmdLine, a:cursorPos)
endfunction

" vim:ft=vim
