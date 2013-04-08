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

if !exists('g:VIdeFlexDocSearchSingleResult')
  " possible values ('open', 'lopen')
  let g:VIdeFlexDocSearchSingleResult = 'open'
endif

if !exists('g:VIdeFlexSearchSingleResult')
  " possible values ('split', 'edit', 'lopen')
  let g:VIdeFlexSearchSingleResult = g:VIdeDefaultFileOpenAction
endif

" ----------------------------------------------------------------------------
"
" Script Variables:
"
" ----------------------------------------------------------------------------

let s:command_search_src = "/flexSearch"
let s:command_search_doc = "/flexDocSearch"

let s:search_element = "<search>?project=<project>&file=<file>&offset=<offset>&length=<length>"
let s:search_pattern = "<search>"
let s:options = ['-p', '-t', '-x', '-s', '-i']
let s:contexts = ['all', 'declarations', 'implementors', 'references']
let s:scopes = ['all', 'project']
let s:types = [
      \ 'metadata',
      \ 'class',
      \ 'field',
      \ 'function',
      \ 'interface',
      \ 'package',
      \ 'type' ]

let s:search_alt_all = '\<<element>\>'
let s:search_alt_references = s:search_alt_all
let s:search_alt_implementors = 
      \ '\(implements\extends\)\_[0-9A-Za-z,[:space:]]*\<<element>\>\_[0-9A-Za-z,[:space:]]*{'

" ----------------------------------------------------------------------------
"
" Script Functions:
"
" ----------------------------------------------------------------------------



" vim:ft=vim
