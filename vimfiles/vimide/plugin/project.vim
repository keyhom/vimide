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
" Command Declartions: 
"
" ----------------------------------------------------------------------------

" Project:, just a specific keyword instead.
command! -nargs=? Project call vimide#project#impl#PrintCurrentProjectName()

" ProjectList:
command! -nargs=? ProjectList call vimide#project#impl#ProjectList('<args>')

" ProjectInfo:
command! -nargs=? 
      \ -complete=customlist,vimide#project#impl#CommandCompleteSingleProject
       \ ProjectInfo call vimide#project#impl#ProjectInfo('<args>')

" ProjectCreate:
command! -nargs=+ 
      \ -complete=customlist,vimide#project#impl#CommandCompleteProjectCreate
      \ ProjectCreate call vimide#project#impl#ProjectCreate('<args>')

" ProjectImport:
command! -nargs=1 -complete=dir
      \ ProjectImport call vimide#project#impl#ProjectImport('<args>')

" ProjectDelete:
command! -nargs=+
      \ -complete=customlist,vimide#project#impl#CommandCompleteMultiProject
      \ ProjectDelete call vimide#project#impl#ProjectDelete(<f-args>)

" ProjectClose:
command! -nargs=+
      \ -complete=customlist,vimide#project#impl#CommandCompleteMultiProject
      \ ProjectClose call vimide#project#impl#ProjectClose(<f-args>)

" ProjectOpen:
command! -nargs=+
      \ -complete=customlist,vimide#project#impl#CommandCompleteMultiProject
      \ ProjectOpen call vimide#project#impl#ProjectOpen(<f-args>)

" vim:ft=vim
