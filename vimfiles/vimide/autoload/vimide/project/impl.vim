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
" Script Variables:
" ----------------------------------------------------------------------------

let s:command_project_by_resource = "/project_by_resource?file=<file>"
let s:command_project_list = "/project_list"
let s:command_project_names = "/project_names"
let s:command_project_info = "/project_info?project=<project>"

" ----------------------------------------------------------------------------
" Functions:
" ----------------------------------------------------------------------------

" Retrieves the project name of current project by the specified file path.
function! vimide#project#impl#GetProject(path)
  if a:path != ''
    let path = vimide#util#LegalPath(a:path)
    let command = substitute(s:command_project_by_resource, '<file>', path, '')
    let result = vimide#Execute(command)
    if string(result) != ''
      return result
    endif
  endif
  return ''
endfunction

" Retrieves the name of current project and print it out.
function! vimide#project#impl#PrintCurrentProjectName()
  let projectName = vimide#project#impl#GetProject(expand('%:p'))
  if projectName != ''
    call vimide#print#EchoInfo("You're just focus in project: " . projectName)
  else
    call vimide#print#EchoWarning("Can't determines the project.")
  endif
endfunction

" Retrieves and print out the supplied or focus project informations.
function! vimide#project#impl#ProjectInfo(project)
  let project = a:project
  if project == ''
    let project = vimide#project#impl#GetProject(expand('%:p'))
  endif
  if project == ''
    call vimide#project#impl#UnableToDetermineProject()
    return
  endif

  let command = substitute(s:command_project_info, '<project>', project, '')
  let result = vimide#Execute(command)

  if type(result) == g:DICT_TYPE
    let output = 
          \ 'Name:      ' . result.name . "\n" . 
          \ 'Path:      ' . result.path . "\n" .
          \ 'Workspace: ' . result.workspace . "\n" .
          \ 'Open:      ' . result.open
    if has_key(result, 'natures')
      let output .= "\n" . 'Natures:   ' . join(result.natures, ', ')
    endif
    if has_key(result, 'depends')
      let output .= "\n" . 'Depends:   ' . join(result.depends, ', ')
    endif
    if has_key(result, 'referenced')
      let output .= "\n" . 'Referenced:' . join(result.referenced, ', ')
    endif
    call vimide#print#Echo(output)
  elseif type(result) == g:STRING_TYPE
    call vimide#print#Echo(result)
  endif
endfunction

" Gets the name collection of the projects.
function! vimide#project#impl#GetProjectNames()
  let command = s:command_project_names
  let result = vimide#Execute(command)
  if type(result) == g:LIST_TYPE
    return result
  else
    call vimide#print#EchoError('Error format.')
  endif
  return []
endfunction

" Retrieves and print out the list of projects.
" @param natures (optional) the natures to filtering. sepa with comma.
function! vimide#project#impl#ProjectList(...)
  let command = s:command_project_list

  let queryString = ''
  let natures = []

  if a:0 > 0
    let natures = split(a:1, ',')
  endif

  for _nature in natures
    if strlen(queryString)
      let queryString .= '&'
    endif
    let queryString .= 'natures=' . _nature
  endfor

  " specific the natures.
  if queryString != ''
    let command .= '?' . queryString
  endif

  let result = vimide#Execute(command)
  if type(result) == g:LIST_TYPE
    " [{
    "   project name:
    "   open status :
    "   project path:
    " }]

    if len(result) == 0
      call vimide#print#EchoInfo("No projects with natures.")
      return
    endif

    let messages = ''
    let index = 1

    for info in result

      let name = get(info, 'name')
      let open = get(info, 'open')
      let path = get(info, 'path')

      let messages .= index . ': ' . name
      if open == 'true'
        let messages .= ' [Opened] '
      else
        let messages .= ' [Closed] '
      endif
      let messages .= "\n  " . path . "\n"

      let index = index + 1
    endfor

    call vimide#print#EchoInfo(messages)
  else
    call vimide#print#EchoError('Error format.')
  endif
endfunction

" Error handling for Unable to determines the supplied project.
function! vimide#project#impl#UnableToDetermineProject()
  call vimide#print#EchoError("Unable to determine the project. " . 
        \ "Please specify a project name or " . 
        \ "execute from a valid project directory.")
endfunction

" Custom command completion for project names.
function! vimide#project#impl#CommandCompleteProject(argLead, cmdLine, cursorPos)
  return vimide#project#impl#CommandCompleteProjectByNature(a:argLead, 
        \ a:cmdLine, a:cursorPos, '')
endfunction

" Custom command completion for project names by natures.
function! vimide#project#impl#CommandCompleteProjectByNature(argLead, cmdLine, cursorPos, nature)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')

  let projects = vimide#project#impl#GetProjectNames()
  if cmdLine !~ '[^\\]\s$'
    let argLead = escape(escape(argLead, '~'), '~')
    " remove escape slashes
    let argLead = substitute(argLead, '\', '', 'g')
    call filter(projects, 'v:val =~ "^' . argLead . '"')
  endif

  call map(projects, 'escape(v:val, " ")')
  return projects
endfunction

" vim:ft=vim
