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
" Script Variables:
"
" ----------------------------------------------------------------------------

let s:required_version = 700

" ----------------------------------------------------------------------------
"
" Script Functions: 
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Determines the vim version.
"
" Validate:
" ----------------------------------------------------------------------------
function! s:Validate() 
  if v:version < s:required_version
    let ver = strpart(v:version, 0, 1) . '.' .  strpart(v:version, 2)
    echom 'Error: your vim version is '. ver . '.'
    echom '       vimide requires version 7.x.x'
    return
  endif
  call s:FeatureValidate()
endfunction 

" ----------------------------------------------------------------------------
" Determines the version and exit early if unsupport vim.
" 
" Early determines for the unsupport vim.
" ----------------------------------------------------------------------------
if v:version < s:required_version
  finish
endif

" ----------------------------------------------------------------------------
" Determines the vim features.
"
" FeatureValidate:
" ----------------------------------------------------------------------------
function! s:FeatureValidate()
  let errors = []
  " Determines 'compatible' option
  if &compatible
    call add(errors, "Error: You have 'compatible' set: ")
    call add(errors, "       Vimide requires 'set nocompatible' in your vimrc.")
    call add(errors, "       Type \":help 'compatible'\" for more details.")
  endif

  "Determines filetype support
  redir => ftsupport
  silent filetype
  redir END
  let ftsupport = substitute(ftsupport, '\n', '', 'g')
  if ftsupport !~ 'detection:ON' || ftsupport !~ 'plugin:ON'
    echo ' '
    let chose = 0
    while string(chose) !~ '1\|2'
      redraw
      echo "Filetype plugin support looks to be disabled, but due to possible"
      echo "language differences, please check the following line manually."
      echo "\n"
      echo '' . ftsupport
      echo "Does it have detection and plugin 'ON'?"
      echo "1) Yes"
      echo "2) No"
      let chose = input("Please choose (1 or 2): ")
    endwhile
    if chose != 1
      call add(errors, "Error: Vimide requires filetype plugin to be enabled.")
      call add(errors, "       Please add 'filetype plugin indent on' to your vimrc.")
      call add(errors, "       Type \":help filetype-plugin-on\" for more details.")
    endif
  endif

  " Print the result.
  redraw
  echohl Statement
  if len(errors) == 0
    echom "Result: OK, required features are valid."
  else
    for error in errors
      echom error
    endfor
  endif
  echohl None
endfunction 

" ----------------------------------------------------------------------------
" Determines the vimide vimfiles baseidr.
"
" VimideBaseDir:
" ----------------------------------------------------------------------------
function! s:VimideBaseDir() 
  if !exists('g:VimideBaseDir')
    let savewig = &wildignore
    set wildignore=""
    let file = findfile('plugin/vimide.vim', escape(&runtimepath, ' '))
    let &wildignore = savewig

    if file == ''
      echom 'Unable to determine vimide basedir. '
      echom '       Pleases report this issus on the vimide user mailing list.'
      let g:VimideBaseDir = ''
      return g:VimideBaseDir
    endif
    let basedir = substitute(fnamemodify(file, ':p:h:h'), '\', '/', 'g')
    let g:VimideBaseDir = escape(basedir, ' ')
  endif
  return g:VimideBaseDir
endfunction

" ----------------------------------------------------------------------------
" Initialized.
"
" Init:
" ----------------------------------------------------------------------------
function! s:Init() 
  let basedir = s:VimideBaseDir()
  if basedir == ''
    return
  endif

  exec 'set runtimepath+=' . basedir . '/vimide,' . basedir . '/vimide/after'
  runtime! vimide/plugin/*.vim
  runtime! vimide/after/plugin/*.vim
endfunction

" Do initialized.
call <SID>Init()

" ----------------------------------------------------------------------------
"
" Autocmd Declarations:
"
" ----------------------------------------------------------------------------
augroup Vimide_autocmd
  au!
  autocmd FileType java nnoremap <silent> <buffer> <leader><leader>j :Comment<cr>
  autocmd FileType java nnoremap <silent> <buffer> <leader><leader>f :%Format<cr>
  autocmd FileType java nnoremap <silent> <buffer> <leader><leader><cr> :Correct<cr>
  autocmd FileType java nnoremap <silent> <buffer> <leader><leader>o :OrganizeImports<cr>
  autocmd FileType java nnoremap <silent> <buffer> <F3> :SearchContext<cr>
  autocmd FileType java nnoremap <silent> <buffer> <F4> :Hierarchy<cr>
augroup END

" ----------------------------------------------------------------------------
"
" Command Declartions:
"
" ----------------------------------------------------------------------------

if !exists(':VIde')
  command VIde :call <SID>Validate()
endif

" vim:ft=vim
