" Author: keyhom (keyhom.c@gmail.com)
" Copyright: 
"

" Global Variables: {{{
let g:NUMBER_TYPE = 0
let g:STRING_TYPE = 1
let g:FUNCREF_TYPE = 2
let g:LIST_TYPE = 3
let g:DICT_TYPE = 4
let g:FLOAT_TYPE = 5

if !exists('g:VimideSeparator')
  let g:VimideSeparator = '/'
  if has('win32') || has('win64')
    let g:VimideSeparator = '\'
  endif
endif

if !exists('g:VimideTempDir')
  let g:VimideTempDir = expand('$TMP')
  if g:VimideTempDir == '$TMP'
    let g:VimideTempDir = expand('$TEMP')
  endif
  if g:VimideTempDir == '$TEMP' && has('unix')
    let g:VimideTempDir = '/tmp'
  endif
  let g:VimideTempDir = substitute(g:VimideTempDir, '\', '/', 'g')
endif

" }}}

" Command Declartions: {{{
if !exists(':PingJface')
  command PingVimide :call vimide#Ping()
endif

if !exists(':VimideDisable')
  command VimideDisable :call <SID>Disable()
endif

if !exists(':VimideEnable')
  command VimideEnable :call <SID>Enable()
endif

" }}}

" Disable the vimide.
function! s:Disable() "{{{
  let g:VimideDisable = 1
endfunction "}}}

" Enable the vimide.
function! s:Enable() "{{{
  let g:VimideDisable = 0
endfunction " }}}




