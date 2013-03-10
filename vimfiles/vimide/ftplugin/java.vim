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
" Misc Settings:
"
" ----------------------------------------------------------------------------
setlocal tw=80
setlocal sw=4
setlocal ts=4

if !exists('g:VIdeJavaValidate')
  let g:VIdeJavaValidate = 1
endif

if !exists('g:VIdeJavaSetCommonOptions')
  let g:VIdeJavaSetCommonOptions = 1
endif

" ----------------------------------------------------------------------------
" 
" Options:
"
" ----------------------------------------------------------------------------

setlocal completefunc=vimide#java#complete#CodeComplete

if g:VIdeJavaSetCommonOptions
  " allow cpp keywords in java files (delete, friend, union, template, etc)
  let java_allow_cpp_keywords = 1

  " tell vim how to search for included files.
  setlocal include=^\s*import
  setlocal includeexpr=substitute(v:fname,'\\.','/','g')
  setlocal suffixesadd=.java
endif

" ----------------------------------------------------------------------------
"
" Autocmds:
"
" ----------------------------------------------------------------------------

if &ft == 'java'
  augroup vimide_java
    autocmd! BufWritePost <buffer>
    autocmd BufWritePost <buffer>
          \ call vimide#lang#UpdateSrcFile('java', g:VIdeJavaValidate)
  augroup end
endif

" ----------------------------------------------------------------------------
"
" Command Declartions:
"
" ----------------------------------------------------------------------------

" Format:
command! -buffer -range Format  :call vimide#java#src#Format(<line1>, <line2>)

" Correct:
command! -buffer Correct  :call vimide#java#correct#Correct()

" Validate:
command! -buffer Validate :call vimide#lang#UpdateSrcFile('java', 1)

" Comment:
command! -buffer Comment :call vimide#java#src#Comment()

" Import:
command! -buffer Import :call vimide#java#src#Import()

" OrganizeImports:
command! -buffer OrganizeImports :call vimide#java#src#OrganizeImports()

" Search:
command! -buffer -nargs=*
      \ -complete=customlist,vimide#java#search#CommandCompleteJavaSearch
      \ Search :call vimide#java#search#SearchAndDisplay('javaSearch', '<args>')

" SearchContext:
command! -buffer SearchContext :call vimide#java#search#SearchAndDisplay('javaSearch', '')

" Constructor:
command! -buffer -range=0 -bang Constructor 
      \ :call vimide#java#impl#GenerateConstructor(<line1>, <line2>, '<bang>')

" vim:ft=vim
