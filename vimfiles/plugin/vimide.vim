" Author: keyhom (keyhom.c@gmail.com)
" Copyright: {{{
"
" }}}

" Command Declartions: {{{
if !exists(':Vimide')
  command Vimide :call <SID>Validate()
endif
" }}}

" Script Variables: {{{
let s:required_version = 700
" }}}

" Script Functions: {{{

" Determines the vim version.
function! s:Validate() " {{{
  if v:version < s:required_version
    let ver = strpart(v:version, 0, 1) . '.' .  strpart(v:version, 2)
    echom 'Error: your vim version is '. ver . '.'
    echom '       vimide requires version 7.x.x'
    return
  endif
  call s:FeatureValidate()
endfunction " }}}

" Determines the version and exit early if unsupport vim.
if v:version < s:required_version
  finish
endif

" Determines the vim features.
function! s:FeatureValidate() "{{{
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
endfunction " }}}

" Determines the vimide vimfiles baseidr.
function! s:VimideBaseDir() " {{{
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
endfunction "}}}

" Initialized.
function! s:Init() " {{{
  let basedir = s:VimideBaseDir()
  if basedir == ''
    return
  endif

  exec 'set runtimepath+=' . basedir . '/vimide,' . basedir . '/vimide/after'
  runtime! vimide/plugin/*.vim
  runtime! vimide/after/plugin/*.vim
endfunction "}}}

" }}}

" Do initialized.
call <SID>Init()

