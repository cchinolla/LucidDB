/*
// $Id$
// Package org.eigenbase is a class library of database components.
// Copyright (C) 2004-2004 Disruptive Tech
// Copyright (C) 2004-2004 John V. Sichi.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
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
package org.eigenbase.util;

import junit.framework.TestCase;
import java.math.*;

/**
 * ReflectVisitorTest tests {@link ReflectUtil.invokeVisitor} and
 * provides a contrived example of how to use it.
 *
 * @author John V. Sichi
 * @version $Id$
 */
public class ReflectVisitorTest extends TestCase
{
    public ReflectVisitorTest(String name)
    {
        super(name);
    }

    /**
     * Tests CarelessNumberNegater.
     */
    public void testCarelessNegater()
    {
        NumberNegater negater = new CarelessNumberNegater();
        Number result;

        // verify that negater is capable of handling integers
        result = negater.negate(new Integer(5));
        assertEquals(-5, result.intValue());
    }

    /**
     * Tests CarefulNumberNegater.
     */
    public void testCarefulNegater()
    {
        NumberNegater negater = new CarefulNumberNegater();
        Number result;

        // verify that negater is capable of handling integers,
        // and that result comes back with same type
        result = negater.negate(new Integer(5));
        assertEquals(-5, result.intValue());
        assertTrue(result instanceof Integer);

        // verify that negater is capable of handling longs;
        // even though it doesn't provide an explicit implementation,
        // it should inherit the one from CarelessNumberNegater
        result = negater.negate(new Long(5));
        assertEquals(-5, result.longValue());
    }

    /**
     * Tests CluelessNumberNegater.
     */
    public void testCluelessNegater()
    {
        NumberNegater negater = new CluelessNumberNegater();
        Number result;
        
        // verify that negater is capable of handling shorts,
        // and that result comes back with same type
        result = negater.negate(new Short((short) 5));
        assertEquals(-5, result.shortValue());
        assertTrue(result instanceof Short);

        // verify that negater is NOT capable of handling integers
        result = negater.negate(new Integer(5));
        assertEquals(null, result);
    }

    /**
     * Tests for ambiguity detection in method lookup.
     */
    public void testAmbiguity()
    {
        NumberNegater negater = new IndecisiveNumberNegater();
        Number result;

        try {
            result = negater.negate(new AmbiguousNumber());
        } catch (IllegalArgumentException ex) {
            // expected
            assertTrue(ex.getMessage().indexOf("ambiguity") > -1);
            return;
        }
        fail("Expected failure due to ambiguity");
    }

    /**
     * NumberNegater defines the abstract base for a computation object
     * capable of negating an arbitrary number.  Subclasses implement
     * the computation by publishing methods with the signature
     * "void visit(X x)" where X is a subclass of Number.
     */
    public abstract class NumberNegater 
    {
        protected Number result;

        /**
         * Negates the given number.
         *
         * @param n the number to be negated
         *
         * @return the negated result; not guaranteed to be the same concrete
         * type as n; null is returend if n's type wasn't handled
         */
        public Number negate(Number n)
        {
            // we specify Number.class as the hierarchy root so
            // that extraneous visit methods are ignored
            result = null;
            ReflectUtil.invokeVisitor(
                this,
                n,
                Number.class,
                "visit");
            return result;
        }
    }

    /**
     * CarelessNumberNegater implements NumberNegater in a careless fashion by
     * converting its input to a double and then negating that.  This can lose
     * precision for types such as BigInteger.
     */
    public class CarelessNumberNegater extends NumberNegater
    {
        public void visit(Number n)
        {
            result = new Double(-n.doubleValue());
        }
    }

    /**
     * CarefulNumberNegater implements NumberNegater in a careful fashion by
     * providing overloads for each known subclass of Number and returning the
     * same subclass for the result.  Extends CarelessNumberNegater so that it
     * can still handle unknown types of Number.
     */
    public class CarefulNumberNegater extends CarelessNumberNegater
    {
        public void visit(Integer i)
        {
            result = new Integer(-i.intValue());
        }
        
        public void visit(Short s)
        {
            result = new Short((short) (-s.shortValue()));
        }

        // ... imagine implementations for other Number subclasses here ...
    }

    /**
     * CluelessNumberNegater implements NumberNegater in a very broken fashion;
     * does the right thing for Shorts, but attempts to override visit(Object).
     * This is just here for testing the hierarchyRoot parameter of
     * invokeVisitor.
     */
    public class CluelessNumberNegater extends NumberNegater
    {
        public void visit(Object obj)
        {
            result = new Integer(42);
        }
        
        public void visit(Short s)
        {
            result = new Short((short) (-s.shortValue()));
        }
    }

    /**
     * IndecisiveNumberNegater implements NumberNegater in such
     * a way that it doesn't know what to do when presented
     * with an AmbiguousNumber.
     */
    public class IndecisiveNumberNegater extends NumberNegater
    {
        public void visit(CrunchableNumber n)
        {
        }
        
        public void visit(FudgeableNumber n)
        {
        }
    }

    /**
     * An interface for introducing ambiguity into the class hierarchy.
     */
    public interface CrunchableNumber
    {
    }

    /**
     * An interface for introducing ambiguity into the class hierarchy.
     */
    public interface FudgeableNumber
    {
    }

    /**
     * A class inheriting two interfaces,  leading to ambiguity.
     */
    public class AmbiguousNumber
        extends BigDecimal implements CrunchableNumber, FudgeableNumber
    {
        AmbiguousNumber()
        {
            super("0");
        }
    }
}

// End ReflectVisitorTest.java