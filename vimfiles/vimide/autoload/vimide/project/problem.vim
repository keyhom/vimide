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

if !exists('g:VideProblemsQuickFixOpen')
  let g:VideProblemsQuickFixOpen = 'botright copen'
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
function! vimide#project#problem#Problems(bang, ...)
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
    let errors = vimide#util#AssembleLocationEntries(result)
  endif

  call vimide#util#SetQuickfixList(errors, 'r')

  " generate a 'signature' to distinguish the problems list from other qf
  " lists.
  " let s:vimide_problems_sig = s:QuickfixSignature()
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
