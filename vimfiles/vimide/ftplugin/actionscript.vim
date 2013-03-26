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
" Settings:
"
" ----------------------------------------------------------------------------
setlocal tw=80
setlocal sw=4
setlocal sw=4
setlocal ci
setlocal syntax=java

if !exists('g:VIdeASValidate')
  let g:VIdeASValidate = 1
endif

if !exists('g:VIdeASPropertyCommonOptions')
  let g:VIdeASPropertyCommonOptions = 1
endif

" ----------------------------------------------------------------------------
"
" Options:
"
" ----------------------------------------------------------------------------

setlocal completefunc=vimide#flex#complete#CodeComplete

if g:VIdeASPropertyCommonOptions
  
  " tell vim how to search for included files.
  setlocal include=^\*import
  setlocal includeexpr=substitute(v:fname,'\\.','/','g')
  setlocal suffixesadd=.as
endif

" ----------------------------------------------------------------------------
"
" Autocmds:
"
" ----------------------------------------------------------------------------

if &ft == 'actionscript'
  augroup vimide_actionscript
    autocmd! BufWritePost <buffer>
    autocmd BufWritePost <buffer>
          \ call vimide#lang#UpdateSrcFile('flex', g:VIdeASValidate, 1)
  augroup END
endif

" ----------------------------------------------------------------------------
"
" Command Declarations:
"
" ----------------------------------------------------------------------------

" Validate:
command! -buffer Validate :call vimide#lang#UpdateSrcFile('flex', 1)

" Format:
command! -buffer -range Format :call vimide#flex#src#Format('<line1>', '<line2>')

" Comment:
command! -buffer Comment :call vimide#flex#src#Comment()

" OrganizeImports:
command! -buffer OrganizeImports :call vimide#flex#src#OrganizeImports()

" vim:ft=vim
