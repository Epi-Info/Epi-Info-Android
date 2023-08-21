package goldengine.java;

/*
 * Licensed Material - Property of Matthew Hawkins (hawkini@barclays.net)
 *
 * GOLDParser - code ported from VB - Author Devin Cook. All rights reserved.
 *
 * No modifications to this code are allowed without the permission of the author.
 */
/**-------------------------------------------------------------------------------------------<br>
 *
 *      Source File:    SymbolTypeConstants.java<br>
 *
 *      Author:         Matthew Hawkins<br>
 *
 *      Description:    A set of constants associated with what type a symbol is.
 *						Do NOT change these numbers!<br>
 *
 *
 *-------------------------------------------------------------------------------------------<br>
 *
 *      Revision List<br>
 *<pre>
 *      Author          Version         Description
 *      ------          -------         -----------
 *      MPH             1.0             First Issue</pre><br>
 *
 *-------------------------------------------------------------------------------------------<br>
 *
 *      IMPORT: NONE<br>
 *
 *-------------------------------------------------------------------------------------------<br>
 */
public interface SymbolTypeConstants
{
    /** Normal nonterminal */
    int symbolTypeNonterminal   = 0;
    /** Normal terminal */
    int symbolTypeTerminal      = 1;
    /** Type of terminal */
    int symbolTypeWhitespace    = 2;
    /** End character (EOF) */
    int symbolTypeEnd           = 3;
    /** Comment start */
    int symbolTypeCommentStart  = 4;
    /** Comment end */
    int symbolTypeCommentEnd    = 5;
    /** Comment line */
    int symbolTypeCommentLine   = 6;
     /** Error symbol */
     int symbolTypeError         = 7;
}