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

let g:NUMBER_TYPE = 0
let g:STRING_TYPE = 1
let g:FUNCREF_TYPE = 2
let g:LIST_TYPE = 3
let g:DICT_TYPE = 4
let g:FLOAT_TYPE = 5

if !exists('g:VIdeLogLevel')
  let g:VIdeLogLevel = 4
endif

if !exists('g:VIdeTraceHighlight')
  let g:VIdeTraceHighlight = 'Normal'
endif

if !exists('g:VIdeDebugHighlight')
  let g:VIdeDebugHighlight = 'Normal'
endif

if !exists('g:VIdeInfoHighlight')
  let g:VIdeInfoHighlight = 'Statement'
endif

if !exists('g:VIdeWarningHighlight')
  let g:VIdeWarningHighlight = 'WarningMsg'
endif

if !exists('g:VIdeErrorHighlight')
  let g:VIdeErrorHighlight = 'Error'
endif

if !exists('g:VIdeFatalHighlight')
  let g:VIdeFatalHighlight = 'Error'
endif

if has('signs')
  if !exists('g:VIdeSignLevel')
    let g:VIdeSignLevel = 5
  endif
else
  let g:VIdeSignLevel = 0
endif

let g:VIdeQuote = "['\"]"

if !exists('g:VIdeSeparator')
  let g:VIdeSeparator = '/'
  if has('win32') || has('win64')
    let g:VIdeSeparator = '\'
  endif
endif

if !exists('g:VIdeTempDir')
  let g:VIdeTempDir = expand('$TMP')
  if g:VIdeTempDir == '$TMP'
    let g:VIdeTempDir = expand('$TEMP')
  endif
  if g:VIdeTempDir == '$TEMP' && has('unix')
    let g:VIdeTempDir = '/tmp'
  endif
  let g:VIdeTempDir = substitute(g:VIdeTempDir, '\', '/', 'g')
endif

if !exists('g:VIdeShowCurrentError')
  let g:VIdeShowCurrentError = 1
endif

if !exists('g:VIdeShowCurrentErrorBalloon')
  let g:VIdeShowCurrentErrorBalloon = 1
endif

if !exists('g:VIdeValidateSortResults')
  let g:VIdeValidateSortResults = 'occurrence'
endif

if !exists('g:VIdeDefaultFileOpenAction')
  let g:VIdeDefaultFileOpenAction = 'split'
endif

if !exists('g:VIdeLocationListHeight')
  let g:VIdeLocationListHeight = 10
endif

" ----------------------------------------------------------------------------
"
" Command Declartions:
"
" ----------------------------------------------------------------------------

if !exists(':PingVIde')
  command PingVIde :call vimide#Ping()
endif

if !exists(':DisableVIde')
  command DisableVIde :call <SID>Disable()
endif

if !exists(':EnableVIde')
  command EnableVIde :call <SID>Enable()
endif

if !exists(':Jobs')
  command Jobs :call vimide#Jobs()
endif

if !exists(':OpenUrl')
  command -bang -range -nargs=? OpenUrl
        \ :call vimide#web#OpenUrl('<args>', '<bang>', <line1>, <line2>)
endif

" ----------------------------------------------------------------------------
"
" Script Functions:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Disable the vimide.
"
" Disable:
" ----------------------------------------------------------------------------
function! s:Disable()
  let g:VIdeDisable = 1
endfunction

" ----------------------------------------------------------------------------
" Enable the vimide.
"
" Enable:
" ----------------------------------------------------------------------------
function! s:Enable()
  let g:VIdeDisable = 0
endfunction

if !exists('g:VIdeDisable')
  let g:VIdeDisable = 0
endif

" vim:ft=vim
