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
package org.eigenbase.rel.metadata;

import org.eigenbase.util14.*;
import org.eigenbase.rel.*;
import org.eigenbase.rel.rules.*;
import org.eigenbase.rex.*;

/**
 * RelMdRowCount supplies a default implementation of
 * {@link RelMetadataQuery#getRowCount} for the standard logical algebra.
 *
 * @author Zelaine Fong
 * @version $Id$
 */
public class RelMdRowCount extends ReflectiveRelMetadataProvider
{
    public Double getRowCount(UnionRelBase rel)
    {
        double nRows = 0.0;
        
        for (RelNode input : rel.getInputs()) {
            Double partialRowCount = RelMetadataQuery.getRowCount(input);
            if (partialRowCount == null) {
                return null;
            }
            nRows += partialRowCount;
        }
        return nRows;
    }
    
    public Double getRowCount(FilterRelBase rel)
    {
        return NumberUtil.multiply( 
            RelMetadataQuery.getSelectivity(
                rel.getChild(), rel.getCondition()),
            RelMetadataQuery.getRowCount(rel.getChild()));
    }
    
    public Double getRowCount(ProjectRelBase rel)
    {
        return RelMetadataQuery.getRowCount(rel.getChild());
    }
    
    public Double getRowCount(SortRel rel)
    {
        return RelMetadataQuery.getRowCount(rel.getChild());
    }
    
    public Double getRowCount(SemiJoinRel rel)
    {
        // create a RexNode representing the selectivity of the
        // semijoin filter and pass it to getSelectivity
        RexNode semiJoinSelectivity = RelMdUtil.makeSemiJoinSelectivityRexNode(
            rel, null);

        return NumberUtil.multiply( 
            RelMetadataQuery.getSelectivity(
                rel.getLeft(), semiJoinSelectivity),
            RelMetadataQuery.getRowCount(rel.getLeft()));
    }
    
    // Catch-all rule when none of the others apply.  Have not implemented
    // rule for aggregation.
    public Double getRowCount(RelNode rel)
    {
        return rel.getRows();
    }
}

// End RelMdRowCount.java