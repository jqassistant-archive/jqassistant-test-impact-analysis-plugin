package org.jqassistant.contrib.plugin.testimpactanalysis.impact.set;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class OtherTypeTest {

    @Test
    public void otherTypeTest() {
        assertThat(new OtherType(), notNullValue());
    }

}
