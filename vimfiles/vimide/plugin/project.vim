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

if !exists('g:VIdeProjectRefreshFiles')
  let g:VIdeProjectRefreshFiles = 1
endif

if !exists('g:VIdeProjectProblemsUpdateOnSave')
  let g:VIdeProjectProblemsUpdateOnSave = 1
endif

if !exists('g:VIdeProjectProblemsUpdateOnBuild')
  let g:VIdeProjectProblemsUpdateOnBuild = 1
endif

" ----------------------------------------------------------------------------
"
" Command Declartions: 
"
" ----------------------------------------------------------------------------

" Project:, just a specific keyword instead.
command! -nargs=? Project :call vimide#project#impl#PrintCurrentProjectName()

" ProjectList:
command! -nargs=? Plist :call vimide#project#impl#ProjectList('<args>')

" ProjectInfo:
command! -nargs=?
      \ -complete=customlist,vimide#project#impl#CommandCompleteSingleProject
      \ Pinfo :call vimide#project#impl#ProjectInfo('<args>')

" Problems:
command! -nargs=* -bang
      \ -complete=customlist,vimide#project#problem#CommandCompleteProblems
      \ Problems :call vimide#project#problem#Problems('<bang>', 1, <f-args>)

" ProjectCreate:
command! -nargs=+ 
      \ -complete=customlist,vimide#project#impl#CommandCompleteProjectCreate
      \ Pcreate :call vimide#project#impl#ProjectCreate('<args>')

" ProjectImport:
command! -nargs=1 -complete=dir
      \ Pimport :call vimide#project#impl#ProjectImport(<f-args>)

" ProjectDelete:
command! -nargs=+
      \ -complete=customlist,vimide#project#impl#CommandCompleteMultiProject
      \ Pdelete :call vimide#project#impl#ProjectDelete(<f-args>)

" ProjectClose:
command! -nargs=+
      \ -complete=customlist,vimide#project#impl#CommandCompleteMultiProject
      \ Pclose :call vimide#project#impl#ProjectClose(<f-args>)

" ProjectOpen:
command! -nargs=+
      \ -complete=customlist,vimide#project#impl#CommandCompleteMultiProject
      \ Popen :call vimide#project#impl#ProjectOpen(<f-args>)

" ProjectRefresh:
command! -nargs=* -bang
      \ -complete=customlist,vimide#project#impl#CommandCompleteMultiProject
      \ Prefresh :call vimide#project#impl#ProjectRefresh('<bang>', <f-args>)

" ProjectCD:
command! -nargs=*
      \ -complete=customlist,vimide#project#impl#CommandCompleteSingleProject
      \ Pcd :call vimide#project#impl#ProjectLCD(<f-args>)

" ProjectBuild:
command! -nargs=* -bang
      \ -complete=customlist,vimide#project#impl#CommandCompleteProjectBuild
      \ Pbuild :call vimide#project#impl#ProjectBuild('<bang>', '<args>')

" vim:ft=vim
