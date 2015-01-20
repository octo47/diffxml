/*
diffxml and patchxml - diff and patch for XML files

Copyright (C) 2002-2009  Adrian Mouat

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

Author: Adrian Mouat
email: adrian.mouat@gmail.com
*/

package org.diffxml.diffxml.fmes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.diffxml.diffxml.DOMOps;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles getting Node sequences and computing LCS on them.
 */
public final class NodeSequence {

  /**
   * Do not allow instantiation.
   */
  private NodeSequence() {
  }

  /**
   * Gets the nodes in set1 which have matches in set2.
   *
   * @param set1      the first set of nodes
   * @param set2      the set of nodes to match against
   * @param matchings the set of matching nodes
   * @return the nodes in set1 which have matches in set2
   */
  public static Node[] getSequence(final NodeList set1, final NodeList set2,
      final NodePairs matchings) {

    Node[] seq = null;
    if (set1 != null && set2 != null) {
      List<Node> resultSet = new ArrayList<Node>(set1.getLength());

      List<Node> set2list = Arrays.asList(
          DOMOps.getElementsOfNodeList(set2));

      for (int i = 0; i < set1.getLength(); i++) {
        if (set2list.contains(matchings.getPartner(set1.item(i)))) {
          resultSet.add(set1.item(i));
        }
      }
      seq = resultSet.toArray(new Node[resultSet.size()]);
    }

    return seq;
  }

  /**
   * Gets the Longest Common Subsequence for the given Node arrays.
   * "Matched" Nodes are considered equal.
   * The returned nodes are from s1.
   * TODO: Check for better algorithms
   *
   * @param s1        First Node sequence
   * @param s2        Second Node sequence
   * @param matchings Set of matching Nodes
   * @return A list of Nodes representing the Longest Common Subsequence
   */
  public static List<Node> getLCS(final Node[] s1, final Node[] s2,
      final NodePairs matchings) {

    int[][] num = new int[s1.length + 1][s2.length + 1];

    for (int i = 1; i <= s1.length; i++) {
      for (int j = 1; j <= s2.length; j++) {
        if (NodeOps.checkIfSameNode(
            matchings.getPartner(s1[i - 1]), s2[j - 1])) {
          num[i][j] = 1 + num[i - 1][j - 1];
        } else {
          num[i][j] = Math.max(num[i - 1][j], num[i][j - 1]);
        }
      }
    }

    //Length of LCS is num[s1.length][s2.length]);

    int s1position = s1.length;
    int s2position = s2.length;

    List<Node> result = new LinkedList<Node>();

    while (s1position != 0 && s2position != 0) {
      if (NodeOps.checkIfSameNode(
          matchings.getPartner(s1[s1position - 1]),
          s2[s2position - 1])) {
        result.add(s1[s1position - 1]);
        s1position--;
        s2position--;
      } else if (num[s1position][s2position - 1]
          >= num[s1position - 1][s2position]) {
        s2position--;
      } else {
        s1position--;
      }
    }

    //TODO: Check if required for our needs
    Collections.reverse(result);

    return result;
  }

}

