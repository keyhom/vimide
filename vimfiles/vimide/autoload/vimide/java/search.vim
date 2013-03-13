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

if !exists("g:VIdeJavaDocSearchSingleResult")
  " possible values ('open', 'lopen')
  let g:VIdeJavaDocSearchSingleResult = 'open'
endif

if !exists('g:VIdeJavaSearchSingleResult')
  " possible values ('split', 'edit', 'lopen')
  let g:VIdeJavaSearchSingleResult = g:VIdeDefaultFileOpenAction
endif

" ----------------------------------------------------------------------------
"
" Script Variables:
"
" ----------------------------------------------------------------------------

let s:command_search_src = "javaSearch"
let s:command_search_doc = "javaDocSearch"

let s:search_element = "<search>?project=<project>&file=<file>&offset=<offset>&length=<length>"
let s:search_pattern = "<search>"
let s:options = ['-p', '-t', '-x', '-s', '-i']
let s:contexts = ['all', 'declarations', 'implementors', 'references']
let s:scopes = ['all', 'project']
let s:types = [
      \ 'annotation',
      \ 'class',
      \ 'classOrEnum',
      \ 'classOrInterface',
      \ 'constructor',
      \ 'enum',
      \ 'field',
      \ 'interface',
      \ 'method',
      \ 'package',
      \ 'type']

let s:search_alt_all = '\<<element>\>'
let s:search_alt_references = s:search_alt_all
let s:search_alt_implementors = 
      \ '\(implements\|extends\)\_[0-9A-Za-z,[:space:]]*\<<element>\>\_[0-9A-Za-z,[:space:]]*{'

" ----------------------------------------------------------------------------
"
" Script Functions:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Builds a pattern based on the cursors current position in the file.
"
" BuildPattern:
" ----------------------------------------------------------------------------
function! s:BuildPattern()
  let class = expand('<cword>')
  " see if the classname element selected is fully qualified.
  let line = getline('.')
  let package = substitute(line, '.*\s\([0-9A-Za-z._]*\)\.' . class . '\>.*', '\1', '')

  " not fully qualified, so attempt to determine package from import.
  if package == line
    let package = vimide#java#util#GetPackageFromImport(class)

    " maybe the element is the current class?
    if package == ''
      if vimide#java#util#GetClassname() == class
        let package = vimide#java#util#GetPackage()
      endif
    endif
  endif

  if package != ''
    return package . '.' . class
  endif

  return class
endfunction

" ----------------------------------------------------------------------------
" Executes a search session.
" Usage closely resebles vimide request client usage.
" When doing a non-pattern search the element under the cursor is searched
" for.
"   Search for declarations of element under the cursor
"     call s:Search("-x", "declarations")
"   Search for references of HashMap
"     call s:Search("-p", "HashM*", "-t", "class", "-x", "references")
" Or all the arguments can be passed in at once:
"   call s:Search("-p 'HashM*' -t class -x references")
"
" Search:
"   command - the search command.
"   [...]   - the arguments.
" ----------------------------------------------------------------------------
function! s:Search(command, ...)
  let argline = ""
  let index = 1
  while index <= a:0
    if index != 1
      let argline = argline . " "
    endif
    let argline .= a:{index}
    let index = index + 1
  endwhile

  " check if pattern supplied without -p.
  if argline !~ '^\s*-[a-z]' && argline !~ '^\s*$'
    let argline = '-p ' . argline
  endif

  let cmdOptions = vimide#util#ToOption(argline)

  let in_project = vimide#project#impl#IsCurrentFileInProject()
  let project = vimide#project#impl#GetProject()
  let file = expand('%:p')
  let pattern = ''
  let type = ''
  let caseSensitive = !&ignorecase
  let scope =  ''
  let offset = 0
  let length = 0
  let context = ''
  let other = ''

  " get pattern
  if has_key(cmdOptions, '-p')
    let pattern = cmdOptions['-p']
  endif

  " get type
  if has_key(cmdOptions, '-t')
    let type = cmdOptions['-t']
  endif

  " get caseSensitive
  if has_key(cmdOptions, '-i')
    let caseSensitive = 1
  endif

  " get scope
  if has_key(cmdOptions, '-s')
    let scope = cmdOptions['-s']
  endif

  " get context
  if has_key(cmdOptions, 'x')
    let context = cmdOptions['-x']
  endif

  let other = "type=" . type . 
        \ "&scope=" . scope . 
        \ "&caseSensitive=" . caseSensitive . 
        \ "&context=" . context . 
        \ "&pattern=" . pattern

  let patternSearch = 0

  " element search
  if argline !~ '-p\>'
    let patternSearch = 0
    if &ft != 'java'
      call vimide#print#EchoWarning("Element searches only supported in java search files.")
      return 0
    endif

    if !vimide#java#util#IsValidIdentifier(expand('<cword>'))
      call vimide#print#EchoError("Element under the cursor is not a valid java identifier.")
      return 0
    endif

    if !in_project
      " build a pattern search and execute it.
      return s:SearchAlternate('-p ' . s:BuildPattern() . ' ' . argline, 1)
    endif

    let position = vimide#util#GetCurrentElementPosition()
    let offset = substitute(position, '\(.*\);\(.*\)', '\1', '')
    let length = substitute(position, '\(.*\);\(.*\)', '\2', '')
  else 
    " Pattern search
    let patternSearch = 1
  endif

  let search_cmd = s:search_element
  let search_cmd = substitute(search_cmd, '<search>', a:command, '')
  let search_cmd = substitute(search_cmd, '<project>', project, '')
  let search_cmd = substitute(search_cmd, '<file>', vimide#util#LegalPath(file, 2), '')
  let search_cmd = substitute(search_cmd, '<offset>', offset, '')
  let search_cmd = substitute(search_cmd, '<length>', length, '')
  let search_cmd .= '&' . other

  let result = vimide#Execute(search_cmd)

  if patternSearch
    if !in_project && filereadable(expand('%'))
      return result + s:SearchAlternate(argline, 0)
    endif
  endif

  return result
endfunction

" ----------------------------------------------------------------------------
" Alternate search for non-project src files using vimgrep and $path.
"
" SearchAlternate:
"   argline - 
"   element -
" ----------------------------------------------------------------------------
function! s:SearchAlternate(argline, element)
  call vimide#print#EchoInfo("Executing alternate search...")
  if a:argline =~ '-t'
    call vimide#print#EchoError("Alternate search doesn't support the type (-t) option yet.")
    return []
  endif

  let search_pattern = ""
  if a:argline =~ '-x all'
    let search_pattern = s:search_alt_all
  elseif a:argline =~ '-x implementors'
    let search_pattern = s:search_alt_implementors
  elseif a:argline =~ '-x references'
    let search_pattern = s:search_alt_references
  endif

  let pattern = substitute(a:argline, '.*-p\s\+\(.\{-}\)\(\s.*\|$\)', '\1', '')
  let file_pattern = substitute(pattern, '\.', '/', 'g') . '.java'

  " search relative to the current dir first.
  let package_path = substitute(vimide#java#util#GetPackage(), '\.', '/', 'g')
  let path = substitute(expand('%:p:h'), '\', '/', 'g')
  let path = substitute(path, package_path, '', '')
  let files = split(vimide#util#Globpath(path, '**/' . file_pattern), '\n')

  " if none found, the search the path.
  if len(files) == 0
    let files = vimide#util#FindFileInPath(file_pattern, 0)
    let path = ""
  endif

  let results = []

  if len(files) > 0 && search_pattern != ''
    " narrow down to, hopefully, a distribution path for a narrower search.
    let response = vimide#util#PromptList(
          \ "Multiple type matches. Pleases choose the relative file.",
          \ files, g:VIdeInfoHighlight)

    if response == -1
      return
    endif

    let file = substitute(get(files, response), '\', '/', 'g')
    if path == ''
      let path = vimide#util#GetPathEntry(file)
    endif

    let path = escape(path, '/\')
    let path = substitute(file, '\(' . path . '[/\\]\?.\{-}[/\\]\).*', '\1', '')
    let pattern = substitute(pattern, '\*', '.\\\\{-}', 'g')
    let search_pattern = substitute(search_pattern, '<element>', pattern, '')
    let command = 'vimgrep /' . search_pattern . '/gj ' . path . '**/*.java'

    silent! exec command

    let loclist = getloclist(0)

    for entry in loclist
      let bufname = bufname(entry.bufnr)
      let result = {
            \ 'filename': bufname,
            \ 'message':  entry.text,
            \ 'line':     entry.lnum,
            \ 'column':   entry.col,
            \ }

      " when searching for implementors, prevent dupes from the somewhat
      " greedy pattern search (may need some more updating post conversion to
      " dict results).
      if a:argline !~ '-x implementors' || !vimide#util#ListContains(results, result)
        call add(results, result)
      endif
    endfor
  endif

  call vimide#print#Echo(' ')
  return results
endfunction

" ----------------------------------------------------------------------------
" Views the supplied file in a browser, or if none proved, the file under the
" cursor.
"
" ViewDoc:
"   [url] - the url which the doc located.
" ----------------------------------------------------------------------------
function! s:ViewDoc(...)
  let url = a:0 > 0 ? a:1 : substitute(getline('.'), '\(.\{-}\)|.*', '\1', '')

  " handle javadocs inside of a jar
  if url =~ '^jar:file:.*!'
    call vimide#Execute(substitute(s:open_doc, '<url>', url, ''))
  else
    cal vimide#web#OpenUrl(url)
  endif
endfunction

" ----------------------------------------------------------------------------
" Executes a search and displays the results via quickfix.
"
" SearchAndDisplay:
"   type  - the type for source.
"   args  - the arguments.
" ----------------------------------------------------------------------------
function! vimide#java#search#SearchAndDisplay(type, args)
  " if running from a non java source file, no SilentUpdate needed.
  if &ft == 'java'
    call vimide#lang#SilentUpdate()
  endif

  let argline = a:args

  " check if just a pattern was supplied.
  if argline =~ '^\s*\w'
    let argline = '-p ' . argline
  endif

  let results = s:Search(a:type, argline)
  if type(results) != g:LIST_TYPE
    return
  endif

  if !empty(results) 
    if a:type == 'javaSearch'
      call vimide#util#SetLocationList(vimide#util#AssembleLocationEntries(results))
      let locs = getloclist(0)
      " if only one result and it's for the current file, just jump to it.
      " note: on windows the expand result must be escaped.
      if len(results) == 1 && locs[0].bufnr == bufnr('%')
        if results[0].line != 1 && results[0].col != 1
          lfirst
        endif
      elseif len(results) == 1 && g:VIdeJavaSearchSingleResult != "lopen"
        " single result in another file.
        let entry = getloclist(0)[0]
        let name = substitute(bufname(entry.bufnr), '\', '/', 'g')
        call vimide#util#GoToBufferWindowOrOpen(name, g:VIdeJavaSearchSingleResult)
        call vimide#util#SetLocationList(vimide#util#AssembleLocationEntries(results))
        call vimide#display#signs#Update()
        call cursor(entry.lnum, entry.col)
      else
        exec 'lopen' . g:VIdeLocationListHeight
      endif
    elseif a:type == 'javaDocSearch'
      let window_name = 'javaDocSearchResults'
      let filename = expand('%:p')
      call vimide#window#TempWindowClear(window_name)

      if len(results) == 1 && g:VIdeJavaDocSearchSingleResult == 'open'
        let entry = results[0]
        call s:ViewDoc(entry)
      else
        call vimide#window#TempWindow(window_name, results, {'height': g:VIdeLocationListHeight})

        nnoremap <silent> <buffer> <cr> :call <SID>ViewDoc()<cr>
        augroup temp_window
          autocmd! BufWinLeave <buffer>
          call vimide#util#GoToBufferWindowRegister(filename)
        augroup END
      endif
    endif
    return 1
  else
    if argline =~ '-p '
      let searchedFor = substitute(argline, '.*-p \(.\{-}\)\( .*\|$\)', '\1', '')
      call vimide#print#EchoInfo("Pattern '" . searchedFor . "' not found.")
    elseif &ft == 'java'
      if !vimide#java#util#IsValidIdentifier(expand('<cword>'))
        return
      endif

      let searchedFor = expand('<cword>')
      call vimide#print#EchoInfo("No results for '" . searchedFor "'.")
    endif
  endif
endfunction

" ----------------------------------------------------------------------------
" Custom command completion for JavaSearch.
"
" CommandCompleteJavaSearch:
"   argLead   - 
"   cmdLine   - 
"   cursorPos -
" ----------------------------------------------------------------------------
function! vimide#java#search#CommandCompleteJavaSearch(argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')

  if cmdLine =~ '-s\s\+[a-z]*$'
    let scopes = deepcopy(s:scopes)
    call filter(scopes, 'v:val =~ "^' . argLead . '"')
    return scopes
  elseif cmdLine =~ '-t\s\+[a-z]*$'
    let types = deepcopy(s:types)
    call filter(types, 'v:val =~ "^' . argLead . '"')
    return types
  elseif cmdLine =~ '-x\s\+[a-z]*$'
    let contexts = deepcopy(s:contexts)
    call filter(contexts, 'v:val =~ "^' . argLead . '"')
    return contexts
  elseif cmdLine =~ '\s\+[-]\?$'
    let options = deepcopy(s:options)
    let index = 0
    for option in options
      if a:cmdLine =~ option
        call remove(options, index)
      else
        let index += 1
      endif
    endfor
    return options
  endif

  return []
endfunction

" ----------------------------------------------------------------------------
" Used by non java source files to find the declaration of a classname under
" the cursor.
"
" FindClassDeclaration:
" ----------------------------------------------------------------------------
function! vimide#java#search#FindClassDeclaration()
  let line = getline('.')
  let class = substitute(line,
        \ '.\{-}\([0-9a-zA-Z_.]*\%' . col('.') . 'c[0-9a-zA-Z_.]*\).*', '\1', '')
  if class != line && class != '' && class =~ '^[a-zA-Z]'
    call vimide#java#search#SearchAndDisplay(
          \ 'javaSearch', '-t classOrInterface -p ' . class)
  endif
endfunction

" vim:ft=vim
