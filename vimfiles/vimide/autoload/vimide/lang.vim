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
" Globals Variables:
" 
" ----------------------------------------------------------------------------

if !exists('g:VIdeSilentRemoteUpdate')
  let g:VIdeSilentRemoteUpdate = 1
endif

" ----------------------------------------------------------------------------
"
" Script Variables:
"
" ----------------------------------------------------------------------------

let s:command_update_src_file = "/<lang>UpdateSrcFile?project=<project>&file=<file>"

" ----------------------------------------------------------------------------
"
" Autoload Functions:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Updates the src file on the server w/ the changes made to the current file.
"
" UpdateSrcFile:
"   lang      - the specific lang of the src file.
"   validate  - the specific flag of validate.
" ----------------------------------------------------------------------------
function! vimide#lang#UpdateSrcFile(lang, validate, ...)
  let file = expand('%:p')
  let project = vimide#project#impl#GetProject(file)
  if '' != project
    let command = s:command_update_src_file
    let command = substitute(command, '<lang>', a:lang, '')
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', vimide#util#LegalPath(file, 2), '')

    if a:validate
      let command .= '&validate=1'
      " if problem list wasn't empty and global prefered to update src file on
      " save.
      let build = vimide#project#problem#IsNotEmpty() && g:VIdeProjectProblemsUpdateOnBuild ? 1 : 0

      if len(a:000) > 0 && a:0 == 0
        let build = 0
      elseif len(a:000) > 0 && a:0 == 1
        let build = 1
      endif

      if build 
        let command .= '&build=1'
      endif
    endif

    let result = vimide#Execute(command)

    if type(result) == g:LIST_TYPE && len(result) > 0
      " Update the quickfix list here.
      let errors = vimide#util#AssembleLocationEntries(result)
      call vimide#util#SetQuickfixList(errors)
    else
      call vimide#util#ClearQuickfixList('global')
    endif

    call vimide#project#problem#ProblemsUpdate('save')
  elseif a:validate && expand('<amatch>') == ''
    call vimide#project#impl#IsCurrentFileInProject()
  endif
endfunction

" ----------------------------------------------------------------------------
" Silently updates the current source file w/out validation.
"
" SilentUpdate: 
"   [temp]
"   [temp_write]
" ----------------------------------------------------------------------------
function! vimide#lang#SilentUpdate(...)
  let pos = getpos('.')
  let file = expand('%:p')
  if file != ''
    try 
      if a:0 && a:1
        " don't create temp files if no server if available to clean them up.
        let project = vimide#project#impl#GetProject(file)
      elseif a:0 < 2 || a:2
        silent noautocmd update!
      endif
    finally
      call setpos('.', pos)
    endtry
  endif
  return vimide#util#LegalPath(file, 2)
endfunction

" ----------------------------------------------------------------------------
" Silently updates the current source file w/out validation.
"
" SilentRemoteUpdate: 
"   [lang]
"   [validate]
"   [temp]
"   [temp_write]
" ----------------------------------------------------------------------------
function! vimide#lang#SilentRemoteUpdate(lang, validate, ...)
  let pos = getpos('.')
  let file = expand('%:p')
  if file != ''
    try 
      if a:0 && a:1
        " don't create temp files if no server if available to clean them up.
        let project = vimide#project#impl#GetProject(file)
      elseif a:0 < 2 || a:2
        silent noautocmd update!
        call vimide#lang#UpdateSrcFile(a:lang, a:validate)
      endif
    finally
      call setpos('.', pos)
    endtry
  endif
  return vimide#util#LegalPath(file, 2)
endfunction

" ----------------------------------------------------------------------------
" Silently writes the current source file w/out validation.
"
" SilentWrite:
"   [temp]
"   [temp_write]
" ----------------------------------------------------------------------------
function! vimide#lang#SilentWrite(...)
  let pos = getpos('.')
  let file = expand('%:p')
  if file != ''
    try 
      if a:0 && a:1
        " don't create temp files if no server if available to clean them up.
        let project = vimide#project#impl#GetProject(file)
      elseif a:0 < 2 || a:2
        silent noautocmd write!
      endif
    finally
      call setpos('.', pos)
    endtry
  endif
  return vimide#util#LegalPath(file, 2)
endfunction

" ----------------------------------------------------------------------------
" Executes the supplied refactoring command bundle error response and
" reloading files that have changed.
"
" Refactor:
"   command - the command
" ----------------------------------------------------------------------------
function! vimide#lang#Refactor(command)
  let cwd = substitute(getcwd(), '\', '/', 'g')
  let cwd_return = 1

  try 
    " turn off swap files temporarily to avoid issues with folder/file
    " renaming.
    let bufend = bufnr('$')
    let bufnum = 1

    while bufnum <= bufend
      if bufexists(bufnum)
        call setbufvar(bufnum, 'save_swapfile', getbufvar(bufnum, '&swapfile'))
        call setbufvar(bufnum, '&swapfile', 0)
      endif
      let bufnum = bufnum + 1
    endwhile

    " cd to the project root to avoid folder renaming issues on windows.
    exec 'cd ' . escape(vimide#util#LegalPath(vimide#project#impl#GetProjectRoot(), 1), ' ')

    let result = vimide#Execute(a:command)
    if type(result) != g:LIST_TYPE && type(result) != g:DICT_TYPE
      return
    endif

    " error occurred 
    if type(result) == g:DICT_TYPE && has_key(result, 'errors')
      call vimide#print#EchoError(result.errors)
      return
    endif

    " reload affected files.
    let curwin = winnr()
    try 
      for info in result
        let newfile = ''
        " handle file renames.
        if has_key(info, 'to')
          let file = info.from
          let newfile = info.to
          if has('win32unix')
            let newfile = vimide#util#LegalPath(newfile, 1)
          endif
        else
          let file = info.file
        endif

        if has('win32unix')
          let file = vimide#util#LegalPath(file, 1)
        endif

        " ignore unchanged directories.
        if isdirectory(file)
          continue
        endif

        " handle correct working directory moved. 
        if newfile != '' && isdirectory(newfile)
          if file =~ '^' . cwd . '\(/\|$\)'
            while cwd !~ '^' . file . '\(/\|$\)'
              let file = fnamemodify(file, ':h')
              let newfile = fnamemodify(newfile, ':h')
            endwhile
          endif

          if cwd =~ '^' . file . '\(/\|$\)'
            let dir = substitute(cwd, file, newfile, '')
            exec 'cd ' . escape(dir, ' ')
            let cwd_return = 0
          endif
          continue
        endif

        let winnr = bufwinnr(file)
        if winnr > -1
          exec winnr . 'winc w'
          if newfile != ''
            let bufnr = bufnr('%')
            enew
            exec 'bdelete ' . bufnr
            exec 'edit ' . escape(vimide#util#Simplify(newfile), ' ')
          else
            call vimide#util#Reload({'retab' : 1})
          endif
        endif
      endfor
    finally
      exec curwin . 'winc w'
      if cwd_return
        exec 'lcd ' . escape(cwd, ' ')
      endif
    endtry
  finally
    " re-enable swap files
    let bufnum = 1
    while bufnum  <= bufend
      if bufexists(bufnum)
        let save_swapfile = getbufvar(bufnum, 'save_swapfile')
        if save_swapfile != ''
          call setbufvar(bufnum, '&swapfile', save_swapfile)
        endif
      endif
      let bufnum = bufnum + 1
    endwhile
  endtry
endfunction

" vim:ft=vim
