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
" Autocmd Declarations:
"
" ----------------------------------------------------------------------------
augroup Vimide_autocmd
  autocmd! FileType actionscript
  autocmd FileType actionscript nnoremap <silent> <buffer> <leader><leader>j :Comment<cr>
  autocmd FileType actionscript nnoremap <silent> <buffer> <leader><leader>f :%Format<cr>
  autocmd FileType actionscript nnoremap <silent> <buffer> <leader><leader><cr> :Correct<cr>
  autocmd FileType actionscript nnoremap <silent> <buffer> <leader><leader>o :OrganizeImports<cr>
  autocmd FileType actionscript nnoremap <silent> <buffer> <F3> :SearchContext<cr>
  autocmd FileType actionscript nnoremap <silent> <buffer> <F4> :Hierarchy<cr>

  autocmd! FileType as3
  autocmd FileType as3 nnoremap <silent> <buffer> <leader><leader>j :Comment<cr>
  autocmd FileType as3 nnoremap <silent> <buffer> <leader><leader>f :%Format<cr>
  autocmd FileType as3 nnoremap <silent> <buffer> <leader><leader><cr> :Correct<cr>
  autocmd FileType as3 nnoremap <silent> <buffer> <leader><leader>o :OrganizeImports<cr>
  autocmd FileType as3 nnoremap <silent> <buffer> <F3> :SearchContext<cr>
  autocmd FileType as3 nnoremap <silent> <buffer> <F4> :Hierarchy<cr>
augroup END


" vim:ft=vim
