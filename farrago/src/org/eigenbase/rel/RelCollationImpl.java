/*
// $Id$
// Package org.eigenbase is a class library of data management components.
// Copyright (C) 2006-2006 The Eigenbase Project
// Copyright (C) 2006-2006 Disruptive Tech
// Copyright (C) 2006-2006 LucidEra, Inc.
//
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version approved by The Eigenbase Project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package org.eigenbase.rel;

import org.eigenbase.reltype.RelDataType;

import java.util.List;
import java.util.Collections;

/**
 * Simple implementation of {@link RelCollation}.
 *
 * @author jhyde
 * @since March 6, 2006
 * @version $Id$
 */
public class RelCollationImpl implements RelCollation
{
    private final List<RelFieldCollation> fieldCollations;

    /**
     * An ordering by the zeroth column.
     */
    public static final List<RelCollation> Singleton0 = createSingleton(0);

    public RelCollationImpl(List<RelFieldCollation> fieldCollations)
    {
        this.fieldCollations = fieldCollations;
    }

    public List<RelFieldCollation> getFieldCollations()
    {
        return fieldCollations;
    }

    public int hashCode()
    {
        return fieldCollations.hashCode();
    }

    public boolean equals(Object obj)
    {
        if (obj instanceof RelCollationImpl) {
            RelCollationImpl that = (RelCollationImpl) obj;
            return this.fieldCollations.equals(that.fieldCollations);
        }
        return false;
    }

    public String toString()
    {
        return fieldCollations.toString();
    }

    /**
     * Creates a list containing one collation containing one field.
     */
    public static List<RelCollation> createSingleton(int fieldIndex)
    {
        return Collections.singletonList(
            (RelCollation) new RelCollationImpl(
                Collections.singletonList(
                    new RelFieldCollation(
                        fieldIndex,
                        RelFieldCollation.Direction.Ascending))));
    }

    /**
     * Checks that a collection of collations is valid.
     *
     * @param rowType Row type of the relational expression
     * @param collationList List of collations
     * @param fail Whether to fail if invalid
     * @return Whether valid
     */
    public static boolean isValid(
        RelDataType rowType, List<RelCollation> collationList, boolean fail)
    {
        final int fieldCount = rowType.getFieldCount();
        for (RelCollation collation : collationList) {
            for (RelFieldCollation fieldCollation : collation.getFieldCollations()) {
                final int index = fieldCollation.getFieldIndex();
                if (index < 0 ||
                    index >= fieldCount) {
                    assert !fail;
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean equal(
        List<RelCollation> collationList1,
        List<RelCollation> collationList2)
    {
        return collationList1.equals(collationList2);
    }
}

// End RelCollationImpl.java